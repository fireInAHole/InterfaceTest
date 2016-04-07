package fire.assist.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import fire.assist.core.service.InterfaceTestService;
import fire.assist.core.util.MyApplicationContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhangcongliang on 16/4/7.
 */
public class InterfaceTestServiceImpl implements InterfaceTestService{

    private static Log log = LogFactory.getLog(InterfaceTestServiceImpl.class);

    private final static String FILE_PATH = "/WEB-INF/views/interfaceTest/interface.txt";

    @Override
    public List<String> getClazzName(String filePath) {
        filePath = filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath;
        File file = new File(filePath+ FILE_PATH);

        if (null == file) {
            log.error("interface configuration document not found!");
            return Collections.emptyList();
        }

        List<String> classNameList = new ArrayList<String>();
        try {

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                classNameList.add(tempString.trim());
            }
        } catch (Exception e) {
            log.error("Load interface configuration document Exception:" + e);
        }

        return classNameList;
    }

    @Override
    public List<String> getMethodListByClazzName(String serviceName) {
        List<String> methodList = new ArrayList<String>();
        try {
            Class cs = Class.forName(serviceName);
            Method[] methods = cs.getMethods();
            if (0 == methods.length)
                return Collections.EMPTY_LIST;

            for(Method method : methods){
                methodList.add(method.getName());
            }
        }catch (ClassNotFoundException e){
            log.error("InterfaceTestServiceImpl getClass by name Exception:" + e);
        }
        return methodList;
    }

    @Override
    public List<Object> getParamByMethod(String clazzName, String methodName) {

        if (StringUtils.isEmpty(clazzName) || StringUtils.isEmpty(methodName)) {
            //TODO log
            return Collections.EMPTY_LIST;
        }

        Class clazz = null;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            //TODO log
        }
        if (null == clazz) {
            //TODO log
            return Collections.EMPTY_LIST;
        }

        Method[] methods = clazz.getMethods();
        if (0 == methods.length) {
            //TODO log
            return Collections.EMPTY_LIST;
        }

        List<Object> paramList = new ArrayList<Object>();

        for (Method method : methods) {
            if (!methodName.equals(method.getName())) {
                continue;
            }
            Class[] paramClass = method.getParameterTypes();

            //TODO 获取参数值
            String[] paramValues = {"1"};
            for (int i = 0; i < paramClass.length; i++) {
                paramList.add(JSONObject.toJavaObject(JSONObject.parseObject(paramValues[i]), paramClass[i]));
            }
        }


        return paramList;
    }

    @Override
    public String getInvokeResults(String clazzName,String methodName,List<Object> params) {
        if(StringUtils.isEmpty(clazzName)
                || StringUtils.isEmpty(methodName)
                || CollectionUtils.isEmpty(params)){
            return "param null";
        }

        Class clazz = null;
        try{
            clazz = Class.forName(clazzName);
        }catch (Exception e){

        }
        if(null == clazz){
            return "class not found :"+clazzName;
        }

        Object objEntity = MyApplicationContextUtil.getContext().getBean(getLowerString(clazzName));

        Object resultObject = new Object();
        Method[] methods = clazz.getMethods();
        try{
            for(Method method : methods){
                if(!method.getName().equals(methodName)){
                    continue;
                }

                resultObject = method.invoke(objEntity, params);
            }
        }catch (Exception e ){
            //TODO log
        }

        return null == resultObject?"":JSONObject.toJSONString(resultObject);
    }

    private String getLowerString(String clazzName){
        String beanName = clazzName.substring(clazzName.lastIndexOf(".")+1);
        char one = beanName.charAt(0);
        beanName = (one+"").toLowerCase()+beanName.substring(1);
        return beanName;
    }
}
