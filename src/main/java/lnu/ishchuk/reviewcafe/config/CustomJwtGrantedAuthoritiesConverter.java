package lnu.ishchuk.reviewcafe.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class CustomJwtGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Map<String, String> ROLE_MAP = Map.of(
            "role-admin", "ROLE_ADMIN",
            "role-user",  "ROLE_USER"
    );

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwt.getClaimAsMap("realm_access").get("roles");
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(ROLE_MAP::get)
                .filter(Objects::nonNull)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
