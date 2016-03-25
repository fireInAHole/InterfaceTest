package fire.assist.core;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by zhangcongliang on 16/3/21.
 */
public class MyApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext context;//声明一个静态变量保存
    public void setApplicationContext(ApplicationContext contex)
            throws BeansException {
        this.context=contex;
    }
    public static ApplicationContext getContext(){
        return context;
    }
}
