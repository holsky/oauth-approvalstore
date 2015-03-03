package com.schmeisky;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessConfirmationController {

    private static final Function<String, String> ADD_SCOPE_PREFIX = new Function<String, String>() {
        @Override
        public String apply(final String scope) {
            return OAuth2Utils.SCOPE_PREFIX + scope;
        }

    };
    
    @RequestMapping("/access_confirmation")
    public String getAccessConfirmation(final Model model, final AuthorizationRequest authorizationRequest) {

        final Iterable<String> scopes =
                FluentIterable.from(authorizationRequest.getScope()) //
                        .transform(ADD_SCOPE_PREFIX)           //
                        .toList();
        model.addAttribute("scopes", scopes);
        model.addAttribute("scope_options", ImmutableSet.of(true, false));
        return "access_confirmation";
    }
}
