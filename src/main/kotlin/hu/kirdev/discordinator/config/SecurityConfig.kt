package hu.kirdev.discordinator.config

import com.fasterxml.jackson.databind.ObjectMapper
import hu.kirdev.discordinator.authsch.ProfileResponse
import hu.kirdev.discordinator.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher
import org.springframework.web.reactive.function.client.WebClient

private const val ROLE_USER = "USER"
private const val ROLE_ADMIN = "ADMIN"

@EnableWebSecurity
@EnableScheduling
@Configuration
class SecurityConfig(
        private val objectMapper: ObjectMapper,
        private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    var authschUserServiceClient = WebClient.builder()
            .baseUrl("https://auth.sch.bme.hu/api")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "AuthSchKotlinAPI")
            .build()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests {
            it.requestMatchers(
                    antMatcher("/"),
                    antMatcher("/identify/**"),
                    antMatcher("/403"),
                    antMatcher("/404"),
                    antMatcher("/error"),
                    antMatcher("/test/**"),
                    antMatcher("/style.css"),
                    antMatcher("/android-chrome-144x144.png"),
                    antMatcher("/favicon.ico"),
                    antMatcher("/favicon-16x16.png"),
                    antMatcher("/favicon-32x32.png"),
                    antMatcher("/noise.png"),
                    antMatcher("/fluid.svg"),
                    antMatcher("/logo.png"),
                    antMatcher("/oauth2/authorization"),
                    antMatcher("/oauth2/authorization/authsch")
            ).permitAll()

            it.requestMatchers(
                    antMatcher("/identify-authsch"),
                    antMatcher("/identified"),
                    antMatcher("/profile"),
                    antMatcher("/profile/change"),
                    antMatcher("/start"),
                    antMatcher("/new-server"),
                    antMatcher("/new-server-steps"),
                    antMatcher("/servers"),
                    antMatcher("/server/**"),
                    antMatcher("/server/**"),
            ).hasRole(ROLE_USER)

            it.requestMatchers(
                    antMatcher("/admin/**")
            ).hasRole(ROLE_ADMIN)
        }
        http.formLogin { it.disable() }
        http.exceptionHandling { it.accessDeniedPage("/403") }
        http.oauth2Login { oauth2 ->
            oauth2.loginPage("/oauth2/authorization/authsch")
                    .userInfoEndpoint { userInfo -> userInfo.userService { resolveAuthschUser(it) } }
                    .defaultSuccessUrl("/identify-authsch")
        }

        return http.build()
    }

    private fun resolveAuthschUser(request: OAuth2UserRequest): DefaultOAuth2User {
        // The API returns `test/json` which is an invalid mime type
        val authschProfileJson: String? = authschUserServiceClient.get()
                .uri { uriBuilder ->
                    uriBuilder.path("/profile/")
                            .queryParam("access_token", request.accessToken.tokenValue)
                            .build()
                }
                .retrieve()
                .bodyToMono(String::class.java)
                .block()

        val profile = objectMapper.readerFor(ProfileResponse::class.java)
                .readValue<ProfileResponse>(authschProfileJson)!!
        val userEntity = userService.getOrCreateUser(profile)

        log.info("User {} logged in (admin:{})", userEntity.fullName, userEntity.admin)
        return DiscoordinatorUser(
                userEntity.id,
                if (userEntity.admin) {
                    mutableListOf(
                            SimpleGrantedAuthority("ROLE_${ROLE_USER}"),
                            SimpleGrantedAuthority("ROLE_${ROLE_ADMIN}"))
                } else {
                    mutableListOf(
                            SimpleGrantedAuthority("ROLE_${ROLE_USER}"))
                },
        )
    }

}