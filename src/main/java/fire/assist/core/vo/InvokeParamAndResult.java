package fire.assist.core.vo;

/**
 * Created by zhangcongliang on 16/4/7.
 */
public class InvokeParamAndResult {
    private Long id;
    //类名
    private String className;
    //方法名
    private String methodName;
    //参数列表
    private String param;
    //参数值
    private String paramValue;
    //执行结果
    private String invokeResult;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getInvokeResult() {
        return invokeResult;
    }

    public void setInvokeResult(String invokeResult) {
        this.invokeResult = invokeResult;
    }
}
