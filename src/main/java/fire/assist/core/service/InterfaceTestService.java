package fire.assist.core.service;

import fire.assist.core.vo.InvokeParamAndResult;

import java.util.List;

/**
 * Created by zhangcongliang on 16/4/7.
 */
public interface InterfaceTestService {

    /**
     * 获取接口名
     * @return
     */
    List<String> getClazzName(String filePath);

    /**
     * 根据类名获取方法名
     * @param serviceName
     * @return
     */
    List<String> getMethodListByClazzName(String serviceName);

    /**
     * 根据类名和参数名获取参数列表
     * @param clazzName
     * @param methodName
     * @return
     */
    List<Object> getParamByMethod(String clazzName,String methodName);

    /**
     * 获取执行结果
     * @return
     */
    String getInvokeResults(String clazzName,String methodName,List<Object> params);

    /**
     * 插入数据库
     * @param invokeParamAndResult
     * @return
     */
    int insert(InvokeParamAndResult invokeParamAndResult);

    /**
     * 根据className+methodName查询得到该接口是否跑过记录
     * @param invokeParamAndResult
     * @return
     */
    InvokeParamAndResult getByClassAndMethodName(InvokeParamAndResult invokeParamAndResult);
}
