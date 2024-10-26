package Team_REAP.appserver.common.login.ano;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)  // 애노테이션을 메서드에 적용
@Retention(RetentionPolicy.RUNTIME)  // 런타임 시점까지 애노테이션이 유지됨
public @interface AuthUser {
}
