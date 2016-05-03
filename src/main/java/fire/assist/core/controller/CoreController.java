package fire.assist.core.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fire.assist.core.service.InterfaceTestService;
import fire.assist.core.util.MyApplicationContextUtil;
import fire.assist.core.vo.InvokeParamAndResult;
import fire.assist.core.vo.InvokeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhangcongliang on 16/3/21.
 */
@Controller
@RequestMapping(value ="/interfaceTest")
public class CoreController {

    @Autowired
    private InterfaceTestService interfaceTestService;


    @RequestMapping(value = "/findTestPage", method = RequestMethod.GET)
    public String findTestPage(HttpServletRequest request, Model model) {
        try {
            //1,获取所有的类名称
            List<String> classNameList = new LinkedList<String>();
            String filePath = request.getSession().getServletContext().getRealPath("")+"/WEB-INF/views/interfaceTest/interface.txt";
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while((tempString = reader.readLine()) != null){
                classNameList.add(tempString.trim());
            }
            if(CollectionUtils.isEmpty(classNameList)){
                //TODO 去录入页面
                request.setAttribute("message", "no class interface data ,check your file.");
                return "/interfaceTest/fail";
            }
            //2,获取第一个类对应的所有方法
            List<String> firstClassMethodList = new LinkedList<String>();
            String className = classNameList.get(0);
            if(StringUtils.isEmpty(className)){
                //TODO 去录入页面
                request.setAttribute("message", "no class interface method data ,check your java jar " + className);
                return "/interfaceTest/fail";
            }
            Class cs = Class.forName(className);
            Method[] method = cs.getMethods();
            for(Method m:method){
                String methodName = m.getName();
                if (!methodName.startsWith("set") && !methodName.startsWith("get") ) {
                    firstClassMethodList.add(m.getName());
                }
            }
            request.setAttribute("classNameList", classNameList);
            request.setAttribute("firstClassMethodList", firstClassMethodList);
            return "/interfaceTest/interface_test";
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", e.getMessage());
            return "/interfaceTest/fail";

        }
    }

    @RequestMapping(value = "/getMethodList", method = RequestMethod.POST)
    public void getMethodList(HttpServletRequest request, HttpServletResponse response){
        try {
            String javaClassName = request.getParameter("javaClassName");
            String message = "";
            Class cs = Class.forName(javaClassName);
            Method[] method = cs.getMethods();
            for(Method m:method){
                message += "<option value="+m.getName()+">"+m.getName()+"</option>";
            }
            ajaxPrintPage(message, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/getParamList", method = RequestMethod.POST)
    public void getParamList(HttpServletRequest request,
                             HttpServletResponse response) {
        try {
            String javaClassName = request.getParameter("javaClassName");
            String javaMethodName = request.getParameter("javaMethodName");
            String message = "";
            Class cs = Class.forName(javaClassName);
            Method[] method = cs.getMethods();
            int i=1;
            for (Method m : method) {
                if (javaMethodName.equals(m.getName())) {
                    Class[] paramClass = m.getParameterTypes();
                    paramClass.getClass().getTypeParameters();
                    m.getGenericParameterTypes();
                    for (Class c : paramClass) {
                        String cName = c.getName();
                        String objectString = "";
                        if (!cName.equals("java.lang.Long") && !cName.equals("long")
                                && !cName.equals("java.lang.Integer") && !cName.equals("int")
                                && !cName.equals("java.lang.String")
                                && !cName.equals("java.util.List")
                                && !cName.equals("java.util.Date")) {

                            GsonBuilder gsonbuilder = new GsonBuilder().serializeNulls();
                            Gson gson = gsonbuilder.create();

                            objectString = gson.toJson(Class.forName(cName).newInstance());
//							objectString = objectString.replaceAll("\"", "\\\\\"");

                            message += "<tr><td style='width:40px'>"
                                    + "第"+i+"个参数"
                                    + ":</td><td><input name='paramClassNames' type='hidden' value='"
                                    + cName
                                    + "'  />   <textarea name='paramValues' cols=40 rows=4>" +
                                    objectString + "</textarea>"
                                    + "&nbsp;*类型:"+cName
                                    + "</td><td style='color:red'>非基础类型请用JSON格式参数</td></tr>";
                        }else{
                            message += "<tr><td style='width:40px'>"
                                    + "第"+i+"个参数"
                                    + ":</td><td><input name='paramClassNames' type='hidden' value='"
                                    + cName
                                    + "'  /><input style='height:100;width:300' name='paramValues' type='text' value='"+objectString+"' />"
                                    + "&nbsp;*类型:"+cName
                                    + "</td><td style='color:red'>非基础类型请用JSON格式参数</td></tr>";
                        }

                        i++;
                    }
                }
            }
            ajaxPrintPage(message, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/invokeInterface", method = RequestMethod.POST)
    @ResponseBody
    public void invokeInterface(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object resultObject = new Object();
        Object result = null;
        try {

            String showType = request.getParameter("showType").trim();
            //1,获取ClassName 信息
            String className = request.getParameter("javaClassName").trim();
            Class cs = Class.forName(className);

            //2,获取方法信息
            String methodName = request.getParameter("javaMethodName").trim();

            //3,获取实例对象
            Object objEntity = MyApplicationContextUtil.getContext().getBean(getLowerString(className));
//			ApplicationContext ac = new FileSystemXmlApplicationContext("classpath*:spring/applicationContext-all.xml");
//			Object objEntity = ac.getBean(getLowerString(className));

            //4,获取参数值、做相应的转化
            String[] paramValues = request.getParameterValues("paramValues");
            String[] paramClassNames = request.getParameterValues("paramClassNames");
            Object[] args = new Object[paramClassNames.length];
            for(int i=0;i<paramClassNames.length;i++) {
                args[i] = getObject(paramClassNames[i],paramValues[i]);
            }

            Method[] methods = cs.getMethods();
            for (Method method:methods) {
                if(method.getName().equals(methodName)){
                    resultObject = method.invoke(objEntity, args);
                }
            }

            if("1".equals(showType)){
                result = getObjStringForRows(resultObject);
            }else if("2".equals(showType)){
                result = getObjStringForColumn(resultObject);
            }else{
                result = resultObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//		System.out.println(request);
//		return result;
        ajaxPrintPage(result, response);
    }

    String getLowerString(String className){
        String beanName = className.substring(className.lastIndexOf(".")+1);
        char one = beanName.charAt(0);
        beanName = (one+"").toLowerCase()+beanName.substring(1);
        return beanName;
    }

    private static Object getObject(String paramClassName,String paramValue){
        if(StringUtils.isEmpty(paramValue)){
            return null;
        }
        Object obj = null;
        try {
            paramClassName = paramClassName.trim();
            paramValue = paramValue.trim();

            if(paramClassName.equals("java.lang.Long")
                    || "long".equals(paramClassName)){
                obj = new Long(paramValue);
            }else if(paramClassName.equals("java.lang.Integer")
                    || "Integer".equals(paramClassName)){
                obj = new Integer(paramValue);
            }else if(paramClassName.equals("java.lang.String")){
                obj = paramValue;
            }else if(paramClassName.equals("java.util.List")){
                JSONArray jsonArray = JSONArray.parseArray(paramValue);
                List<Object> list = new ArrayList<Object>();
                for (int i = 0; i < jsonArray.size(); i++)
                    list.add(jsonArray.getJSONObject(i));
                return list;
            }else if(paramClassName.equals("java.util.Date")){
                obj = getDateFromString(paramValue);
            }else{
                JSONObject jo = JSONObject.parseObject(paramValue);
                try {
                    obj = JSONObject.toJavaObject(jo, Class.forName(paramClassName));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        return obj;
    }

    private String getObjStringForRows(Object obj){
        try {
            String table = "";
            Class cs = obj.getClass();
            Method[] methods = cs.getMethods();
            for(Method m:methods){
                String methodName = m.getName();
                if(methodName.startsWith("getOut")){
                    Object out = m.invoke(obj, null);
                    System.out.println("method:"+m.getName()+",obj"+ JSON.toJSONString(obj)+",out:"+out);
                    if(out == null){
                        table=getObjStringError(obj);
                        return table;
                    }
                    if(out instanceof List){
                        List list = (List)out;
                        int length = list.size();
                        if(length<1){
                            return "no value";
                        }
                        List<List<String>> listAll = new LinkedList<List<String>>();
                        for (int i=0;i<length;i++) {
                            Object cobj = list.get(i);
                            Class csout = cobj.getClass();
                            Method[] childMethods = csout.getMethods();
                            //System.out.println("i="+i);
                            List<String> aa = new LinkedList<String>();
                            for(Method m1:childMethods){
                                String methodName1 = m1.getName();
                                if (methodName1.startsWith("get")) {
                                    Object object =null;
                                    try{
                                        object = m1.invoke(cobj, null);
                                    }catch (Exception e){

                                    }
                                    if(null == object){
                                        continue;
                                    }
                                    if(length == 1){
                                        aa.add("<tr><td>"+methodName1.substring(3)+"</td><td>"+object+"</td><tr>");
                                    }else if (i==0) {
                                        aa.add("<tr><td>"+methodName1.substring(3)+"</td><td>"+object+"</td>");
                                    }else if(i==length-1){
                                        aa.add("<td>"+object+"</td></tr>");
                                    }else{
                                        aa.add("<td>"+object+"</td>");
                                    }
                                }
                            }
                            listAll.add(aa);
                        }
                        List<String> listStr = listAll.get(0);
                        for (int n=0;n<listStr.size();n++) {
                            for (int i=0;i<length;i++) {
                                table += listAll.get(i).get(n);
                            }
                        }
                        return table;
                    }else{
                        Class csout = out.getClass();
                        Method[] childMethods = csout.getMethods();
                        for(Method m1:childMethods){
                            String methodName1 = m1.getName();
                            if (methodName1.startsWith("get")) {
                                table += "<tr><td>"+methodName1.substring(3)+"</td>";
                                table += "<td>"+m1.invoke(out, null)+"</td></tr>";
                            }
                        }
                    }
                }
            }
            return table;


        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getObjStringForColumn(Object obj){
        try {
            String firstRow="";
            String table = "";
            Class cs = obj.getClass();
            Method[] methods = cs.getMethods();
            for(Method m:methods){
                String methodName = m.getName();
                if(methodName.startsWith("getOut")){
                    Object out = m.invoke(obj, null);
                    if(out == null){
                        table=getObjStringError(obj);
                        return table;
                    }
                    if(out instanceof List){
                        List list = (List)out;
                        int length = list.size();
                        if(length<1){
                            return "no value";
                        }
                        for (int i=0;i<length;i++) {
                            Object cobj = list.get(i);
                            Class csout = cobj.getClass();
                            Method[] childMethods = csout.getMethods();
                            //System.out.println("i="+i);
                            List<String> aa = new LinkedList<String>();
                            table += "<tr>";
                            for(Method m1:childMethods){
                                String methodName1 = m1.getName();
                                if (methodName1.startsWith("get")) {
                                    Object object =null;
                                    try{
                                        object = m1.invoke(cobj, null);
                                    }catch (Exception e){

                                    }
                                    if(null == object){
                                        continue;
                                    }
                                    if(i==0){
                                        firstRow += "<td>"+methodName1.substring(3)+"</td>";
                                        table += "<td>"+object+"</td>";
                                    }else{
                                        table += "<td>"+object+"</td>";
                                    }
                                }
                            }
                            table += "</tr>";
                        }
                    }else{
                        Class csout = out.getClass();
                        Method[] childMethods = csout.getMethods();
                        table += "<tr>";
                        for(Method m1:childMethods){
                            String methodName1 = m1.getName();
                            if (methodName1.startsWith("get")) {
                                firstRow += "<td>"+methodName1.substring(3)+"</td>";
                                table += "<td>"+m1.invoke(out, null)+"</td>";
                            }
                        }
                        table += "</tr>";
                    }
                }
            }
            table = "<tr>"+firstRow+"</tr>"+table;
            return table;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getObjStringError(Object obj){
        try {
            JSONObject json = new JSONObject();
            json.put("obj", obj);
            return json.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Date getDateFromString(String dataString){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = df.parse(dataString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
    /**
     * ajax请求输出
     *
     * @param obj
     */
    public void ajaxPrintPage(Object obj,  HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = null;
        try {
            try {
                Gson gson = new Gson();
                writer = response.getWriter();
                writer.print(gson.toJson(obj));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }



    @RequestMapping(value = "/addParam", method = RequestMethod.POST)
    @ResponseBody
    public String addParam(HttpServletRequest request, HttpServletResponse response){
        String javaClassName = request.getParameter("javaClassName");

        Class cs = null;
        try {
            cs = Class.forName(javaClassName);
        }catch (ClassNotFoundException e){
        }
        String javaMethodName = request.getParameter("javaMethodName");
        String[] paramValues = request.getParameterValues("paramValues");
        String[] paramClassNames = request.getParameterValues("paramClassNames");

        Object[] args = new Object[paramClassNames.length];
        for(int i=0;i<paramClassNames.length;i++) {
            args[i] = getObject(paramClassNames[i],paramValues[i]);
        }

        Object objEntity = MyApplicationContextUtil.getContext().getBean(getLowerString(javaClassName));

        Method[] methods = cs.getMethods();
        Object resultObj = new Object();
        try{
            for (Method method:methods) {
                if(method.getName().equals(javaMethodName)){
                    resultObj = method.invoke(objEntity, args);
                }
            }
        }catch (Exception e){
            resultObj = e;
        }

        InvokeParamAndResult invokeParamAndResult = new InvokeParamAndResult();
        invokeParamAndResult.setClassName(javaClassName);
        invokeParamAndResult.setMethodName(javaMethodName);
        invokeParamAndResult.setParam(JSONObject.toJSONString(paramClassNames));
        invokeParamAndResult.setParamValue(JSONObject.toJSONString(paramValues));
        //TODO 未来对结果要做截断，可能会超长
        invokeParamAndResult.setInvokeResult(JSONObject.toJSONString(resultObj));
        interfaceTestService.insert(invokeParamAndResult);

        return JSON.toJSONString("ok");
    }

    final int cores = Runtime.getRuntime().availableProcessors();
    private ExecutorService executors = Executors.newFixedThreadPool(cores + 2);

    @RequestMapping(value = "/invokeAll",method = RequestMethod.POST)
    @ResponseBody
    public String executeAllTestAutoly(HttpServletRequest request){
        //获取类名
        List<String> clazzList = interfaceTestService.getClazzName(request.getSession().getServletContext().getRealPath(""));
        Map<String,List<String>> clazzMethodMap = new HashMap<>();
        for(String clazz : clazzList){
            List<String> methodList = interfaceTestService.getMethodListByClazzName(clazz);
            if(CollectionUtils.isEmpty(methodList)){
                continue;
            }
            clazzMethodMap.put(clazz, methodList);
        }

        //执行结果
        List<InvokeResult> invokeResults = new ArrayList<>();

        InvokeParamAndResult invokeParamAndResult= new InvokeParamAndResult();
        //执行
        for(Map.Entry<String,List<String>> entry : clazzMethodMap.entrySet()){
            String clazzName = entry.getKey();
            List<String> methodList = entry.getValue();
            invokeParamAndResult.setClassName(clazzName);
            for(String methodName : methodList){
                invokeParamAndResult.setMethodName(methodName);
                InvokeParamAndResult record = interfaceTestService.getByClassAndMethodName(invokeParamAndResult);

                InvokeResult invokeResult = new InvokeResult();
                invokeResult.setClazzName(clazzName);
                invokeResult.setMethodName(methodName);
                if(null == record){
                    invokeResult.setIsRecordExists(InvokeResult.RECODE_NOT_EXISTS);
                    invokeResults.add(invokeResult);
                    continue;
                }
                List<Object> paramList = interfaceTestService.getParamByMethod(clazzName,methodName);
                String result = interfaceTestService.getInvokeResults(clazzName, methodName, paramList);

                invokeResult.setExecuteParams(JSONObject.toJSONString(paramList));
                invokeResult.setOrginResults(record.getInvokeResult());
                invokeResult.setExecuteResults(result);
                //TODO 这个判等有NullPointer风险
                invokeResult.setIsEqual(result.equals(record.getInvokeResult()) ? InvokeResult.RESULT_EQUAL : InvokeResult.RESULT_NOT_EQUAL);
                invokeResult.setIsRecordExists(InvokeResult.RECODE_EXISTS);
                invokeResults.add(invokeResult);
            }
        }

        return JSONObject.toJSONString(invokeResults);
    }
}
