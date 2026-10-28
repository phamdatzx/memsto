package dev.memsto.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import dev.memsto.TestcontainersConfig;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class AuthFlowTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SessionCookies cookies;

    Cookie xsrf;
    String email;

    @BeforeEach
    void fetchCsrfToken() throws Exception {
        xsrf = mvc.perform(get("/api/auth/csrf")).andExpect(status().isNoContent())
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrf).isNotNull();
        email = "user-" + UUID.randomUUID() + "@example.com";
    }

    MockHttpServletRequestBuilder postJson(String url, String body) {
        return post(url).contentType(MediaType.APPLICATION_JSON).content(body)
                .cookie(xsrf).header("X-XSRF-TOKEN", xsrf.getValue());
    }

    String creds(String email, String password) {
        return """
                {"email":"%s","password":"%s","displayName":"Meme Lord"}""".formatted(email, password);
    }

    @Test
    void registerLoginMeLogout() throws Exception {
        MvcResult reg = mvc.perform(postJson("/api/auth/register", creds(email, "hunter22!")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Meme Lord"))
                .andReturn();
        Cookie session = reg.getResponse().getCookie(cookies.name());
        assertThat(session.isHttpOnly()).isTrue();

        mvc.perform(get("/api/auth/me").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // Email is matched case-insensitively.
        mvc.perform(postJson("/api/auth/login", creds(email.toUpperCase(), "hunter22!")))
                .andExpect(status().isOk());

        MvcResult out = mvc.perform(postJson("/api/auth/logout", "").cookie(session))
                .andExpect(status().isNoContent()).andReturn();
        assertThat(out.getResponse().getCookie(cookies.name()).getMaxAge()).isZero();
    }

    @Test
    void rejectsDuplicateEmailAndWrongPassword() throws Exception {
        mvc.perform(postJson("/api/auth/register", creds(email, "hunter22!"))).andExpect(status().isCreated());
        mvc.perform(postJson("/api/auth/register", creds(email, "hunter22!"))).andExpect(status().isConflict());
        mvc.perform(postJson("/api/auth/login", creds(email, "wrong-pass"))).andExpect(status().isUnauthorized());
        mvc.perform(postJson("/api/auth/login", creds("nobody@example.com", "hunter22!")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validatesInput() throws Exception {
        mvc.perform(postJson("/api/auth/register", creds("not-an-email", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void requiresCsrfTokenForPosts() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(creds(email, "x")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousAndInvalidSessionsAreUnauthorized() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());

        // A bad cookie is cleared but doesn't block public endpoints.
        Cookie bogus = new Cookie(cookies.name(), "garbage");
        MvcResult res = mvc.perform(get("/api/auth/me").cookie(bogus)).andExpect(status().isUnauthorized()).andReturn();
        assertThat(res.getResponse().getCookie(cookies.name()).getMaxAge()).isZero();
        mvc.perform(postJson("/api/auth/login", creds(email, "hunter22!")).cookie(bogus, xsrf))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void pingIsPublic() throws Exception {
        mvc.perform(get("/api/ping")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }
}
