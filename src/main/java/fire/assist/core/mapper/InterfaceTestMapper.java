package fire.assist.core.mapper;

import fire.assist.core.vo.InvokeParamAndResult;

/**
 * Created by zhangcongliang on 16/5/3.
 */
public interface InterfaceTestMapper {

    int insert(InvokeParamAndResult invokeParamAndResult);

    InvokeParamAndResult getByClassAndMethodName(InvokeParamAndResult invokeParamAndResult);
}
