package fire.assist.core.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fire.assist.core.mapper.InterfaceTestMapper;
import fire.assist.core.service.InterfaceTestService;
import fire.assist.core.util.MyApplicationContextUtil;
import fire.assist.core.vo.InvokeParamAndResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhangcongliang on 16/4/7.
 */
@Service(value = "interfaceTestService")
public class InterfaceTestServiceImpl implements InterfaceTestService{

    private static Log log = LogFactory.getLog(InterfaceTestServiceImpl.class);

    private final static String FILE_PATH = "/WEB-INF/views/interfaceTest/interface.txt";

    @Autowired
    private InterfaceTestMapper interfaceTestMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public List<String> getClazzName(String filePath) {
        filePath = filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath;
        File file = new File(filePath + FILE_PATH);

        if (null == file) {
            log.error("interface configuration document not found!");
            return Collections.emptyList();
        }

        List<String> classNameList = new ArrayList<>();
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
        List<String> methodList = new ArrayList<>();
        try {
            Class cs = Class.forName(serviceName);
            Method[] methods = cs.getMethods();
            if (0 == methods.length)
                return Collections.EMPTY_LIST;

            for (Method method : methods) {
                methodList.add(method.getName());
            }
        } catch (ClassNotFoundException e) {
            log.error("InterfaceTestServiceImpl getClass by name Exception:" + e);
        }
        return methodList;
    }

    @Override
    public List<Object> getParamByMethod(String clazzName, String methodName) {

        if (StringUtils.isEmpty(clazzName) || StringUtils.isEmpty(methodName)) {
            return Collections.EMPTY_LIST;
        }

        Class clazz = null;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
        }
        if (null == clazz) {
            return Collections.EMPTY_LIST;
        }

        Method[] methods = clazz.getMethods();
        if (0 == methods.length) {
            return Collections.EMPTY_LIST;
        }

        List<Object> paramList = new ArrayList<>();
        InvokeParamAndResult invokeParamAndResult = new InvokeParamAndResult();
        invokeParamAndResult.setClassName(clazzName);

        InvokeParamAndResult record;
        String paramString;
        for (Method method : methods) {
            if (!methodName.equals(method.getName())) {
                continue;
            }
            Class[] paramClass = method.getParameterTypes();

            invokeParamAndResult.setMethodName(method.getName());
            record = interfaceTestMapper.getByClassAndMethodName(invokeParamAndResult);
            if (null == record)
                continue;
            paramString = record.getParamValue();
            //TODO 对paramString做分离，组装成param数组
            //TODO 采用,分隔其实不对，后续再考虑怎么进行分隔
            //TODO 不论采用什么分隔符，要在新增、读取、解析的位置都做处理，避免不一致导致的问题
            JSONArray jsonArray = JSONArray.parseArray(paramString);
//            String[] paramValues = paramString.split(",");
            for (int i = 0; i < paramClass.length; i++) {
                paramList.add(getObject(paramClass[i].getName(), jsonArray.getString(i)));
            }
        }

        return paramList;
    }

    @Override
    public InvokeParamAndResult getByClassAndMethodName(InvokeParamAndResult invokeParamAndResult){
        //TODO 未来可能存在一个接口对应多个测试case，那么这个查询就会异常
        return interfaceTestMapper.getByClassAndMethodName(invokeParamAndResult);
    }

    private static Object getObject(String paramClassName, String paramValue) {
        if (org.springframework.util.StringUtils.isEmpty(paramValue)) {
            return null;
        }
        Object obj = null;
        try {
            paramClassName = paramClassName.trim();
            paramValue = paramValue.trim();

            if (paramClassName.equals("java.lang.Long")
                    || "long".equals(paramClassName)) {
                obj = new Long(paramValue);
            } else if (paramClassName.equals("java.lang.Integer")
                    || "int".equals(paramClassName)) {
                obj = new Integer(paramValue);
            } else if (paramClassName.equals("java.lang.String")) {
                obj = paramValue;
            } else if (paramClassName.equals("java.util.List")) {
                JSONArray jsonArray = JSONArray.parseArray(paramValue);
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++)
                    list.add(jsonArray.getJSONObject(i));
                return list;
            } else if (paramClassName.equals("java.util.Date")) {
                obj = getDateFromString(paramValue);
            } else {
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

    private static Date getDateFromString(String dataString) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = df.parse(dataString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public String getInvokeResults(String clazzName, String methodName, List<Object> params) {
        if (StringUtils.isEmpty(clazzName)
                || StringUtils.isEmpty(methodName)
                || CollectionUtils.isEmpty(params)) {
            return "param null";
        }

        Class clazz = null;
        try {
            clazz = Class.forName(clazzName);
        } catch (Exception e) {
        }
        if (null == clazz) {
            return "class not found :" + clazzName;
        }

        Object objEntity = MyApplicationContextUtil.getContext().getBean(getLowerString(clazzName));
        Object resultObject = new Object();
        Method[] methods = clazz.getMethods();
        try {
            for (Method method : methods) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }

                HashMap<String, Object> resultMap = new HashMap<>();
                Object[] args = new Object[params.size()];
                for(int i=0;i<params.size();i++){
                    args[i]=params.get(i);
                }
                invokeMethod(method, objEntity, args, resultMap);
                resultObject = resultMap.get(method.getName());
                break;
            }
        } catch (Exception e) {
        }

        return null == resultObject ? "" : JSONObject.toJSONString(resultObject);
    }


    /**
     * 每次执行必定回滚，这样可以让数据一直有效，不用回滚数据库
     * //TODO 待验证是否有效
     *
     * @param method
     * @param objEntity
     * @param params
     * @param resultMap
     */
    private void invokeMethod(Method method,
                              Object objEntity,
                              Object[] params,
                              HashMap<String, Object> resultMap) {
        Exception exception = transactionTemplate.execute(transactionStatus -> {
            try {
                Object resultObject = method.invoke(objEntity, params);
                resultMap.put(method.getName(), resultObject);
                throw new TestException();
            } catch (TestException ex) {
                transactionStatus.setRollbackOnly();
                return null;
            } catch (Throwable th) {
                log.error("##########################################",th);
                resultMap.put(method.getName(), th);
                transactionStatus.setRollbackOnly();
                return new Exception(th);
            }
        });
    }

    private String getLowerString(String clazzName) {
        String beanName = clazzName.substring(clazzName.lastIndexOf(".") + 1);
        char one = beanName.charAt(0);
        beanName = (one + "").toLowerCase() + beanName.substring(1);
        return beanName;
    }

    @Override
    @Transactional
    public int insert(InvokeParamAndResult invokeParamAndResult) {
        return interfaceTestMapper.insert(invokeParamAndResult);
    }


    private class TestException extends RuntimeException {

    }
}
