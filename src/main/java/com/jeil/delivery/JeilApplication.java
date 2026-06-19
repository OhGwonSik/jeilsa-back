package com.jeil.delivery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Import({
        com.common.auth.auth.controller.AuthController.class,
        com.common.auth.auth.service.AuthService.class,       // (서비스도 함께 보장)
        com.common.auth.common.util.RequestUtil.class,         // (@Component면 자동, 아니면 생략 가능)
        com.common.auth.auth.service.RefreshTokenService.class,
        com.common.auth.user.service.UserService.class,
        com.common.auth.user.service.UserGridService.class,
        com.common.auth.menu.service.MenuService.class,
        com.common.auth.common.security.PermissionHelper.class,
        com.common.auth.role.controller.RoleController.class,
        com.common.auth.role.controller.RoleGridController.class,
        com.common.auth.role.service.RoleService.class,
        com.common.auth.role.service.RoleGridService.class,
        com.common.auth.permission.controller.PermissionController.class,
        com.common.auth.rolepermission.controller.RolePermissionGridController.class,
        com.common.auth.rolepermission.service.RolePermissionGridService.class,
        com.common.auth.rolemenu.controller.RoleMenuGridController.class,
        com.common.auth.rolemenu.service.RoleMenuGridService.class,
        com.common.auth.permission.service.PermissionService.class,
        com.common.auth.menu.controller.MenuGridController.class,
        com.common.auth.menu.service.MenuGridService.class
})
// 공통 빈도 스캔
@SpringBootApplication(scanBasePackages = {
        "com.jeil.delivery",
        "com.common.auth"      // auth 전체 스캔
})
@ComponentScan(
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = com.common.auth.common.config.DatabaseConfig.class
                )
        }
)
@MapperScan({
        "com.jeil.delivery.**.mapper",
        "com.common.auth.**.mapper" // auth 매퍼까지 읽기
})
public class JeilApplication {
    public static void main(String[] args) {
        SpringApplication.run(JeilApplication.class, args);
    }
}
