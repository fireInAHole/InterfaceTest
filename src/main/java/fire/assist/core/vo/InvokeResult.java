package fire.assist.core.vo;

/**
 * Created by zhangcongliang on 16/4/7.
 */
public class InvokeResult {
    //运行结果不相同
    public static final int RESULT_NOT_EQUAL = 0;
    //运行结果相同
    public static final int RESULT_EQUAL = 1;
    //不存在记录
    public static final int RECODE_NOT_EXISTS = 0;
    //存在记录
    public static final int RECODE_EXISTS = 1;

    //类名
    private String clazzName;
    //方法名
    private String methodName;
    //执行参数
    private String executeParams;
    //执行结果
    private String executeResults;
    //原结果
    private String orginResults;
    //是否一致，0:不一致，1:一致
    private int isEqual;
    //是否存在运行记录，0:不存在，1：存在
    private int isRecordExists;

    public String getClazzName() {
        return clazzName;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getExecuteParams() {
        return executeParams;
    }

    public void setExecuteParams(String executeParams) {
        this.executeParams = executeParams;
    }

    public String getExecuteResults() {
        return executeResults;
    }

    public void setExecuteResults(String executeResults) {
        this.executeResults = executeResults;
    }

    public String getOrginResults() {
        return orginResults;
    }

    public void setOrginResults(String orginResults) {
        this.orginResults = orginResults;
    }

    public int getIsEqual() {
        return isEqual;
    }

    public void setIsEqual(int isEqual) {
        this.isEqual = isEqual;
    }

    public int getIsRecordExists() {
        return isRecordExists;
    }

    public void setIsRecordExists(int isRecordExists) {
        this.isRecordExists = isRecordExists;
    }
}
