import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by zhangcongliang on 16/4/7.
 */
public class InterfaceTestService {
    @Autowired
    private InterfaceTestService interfaceTestService;

    private void print(Object o){
        System.out.println("###################print start#####################");
        System.out.println(JSONObject.toJSONString(o));
        System.out.println("###################print end#####################");
    }

    @Test
    public void testGetClazzName(){
//        this.getClass().getResource("").getPath();
        String filePath = "/Users/zhangcongliang/IdeaProjects/activity/activity-server/src/main/webapp/";
        filePath = filePath.endsWith("/") ? filePath.substring(0, filePath.length() - 1) : filePath;
        print(filePath);
        List<String> clazzList = interfaceTestService.getClazzName(filePath);
        print(clazzList);

        String clazzName = clazzList.get(0);
        String methodName = interfaceTestService.getMethodListByClazzName(clazzName).get(0);
        print(methodName);

        List<Object> params = interfaceTestService.getParamByMethod(clazzName,methodName);
        print(params);

    }
}
