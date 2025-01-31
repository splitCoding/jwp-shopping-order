package cart.config;

import cart.dao.MemberDao;
import cart.ui.MemberArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final MemberDao memberDao;

    public WebMvcConfig(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000",
                "http://218.39.176.142:3000",
                "https://woowasplit.shop/",
                "https://react-shopping-cart-woowa.netlify.app/",
                "https://react-shopping-cart-prod-6izahtdpl-shackstack.vercel.app/",
                "https://shackstack-tiffany.vercel.app/"
            )
            .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTION")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new MemberArgumentResolver(memberDao));
    }
}
