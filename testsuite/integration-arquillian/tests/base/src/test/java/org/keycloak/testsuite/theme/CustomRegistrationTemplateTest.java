/*
 *  Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.keycloak.testsuite.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.arquillian.graphene.page.Page;
import org.junit.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.testsuite.AbstractTestRealmKeycloakTest;
import org.keycloak.testsuite.pages.AppPage;
import org.keycloak.testsuite.pages.LoginPage;
import org.keycloak.testsuite.pages.RegisterPage;

public class CustomRegistrationTemplateTest extends AbstractTestRealmKeycloakTest {

    static final Map<String, String> CUSTOM_ATTRIBUTES = Map.of("street", "street",
            "locality",
            "locality",
            "region", "region",
            "postal_code", "postal_code",
            "country", "country");

    @Page
    protected LoginPage loginPage;
    @Page
    protected RegisterPage registerPage;
    @Page
    protected AppPage appPage;

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {
        testRealm.setRegistrationAllowed(true);
        testRealm.setLoginTheme("address");
    }

    @Test
    public void testRegistration() {
        //contains few special characters we want to be sure they are allowed in username
        UserRepresentation user = register();
        Map<String, List<String>> attributes = user.getAttributes();
        assertFalse(attributes.isEmpty());
        assertCustomAttributes(attributes);
    }

    protected static void assertCustomAttributes(Map<String, List<String>> attributes) {
        for (Entry<String, String> attribute : CUSTOM_ATTRIBUTES.entrySet()) {
            String name = attribute.getKey();
            List<String> values = attributes.get(name);
            assertNotNull(values);
            assertFalse(values.isEmpty());
            assertEquals(CUSTOM_ATTRIBUTES.get(name), values.get(0));
        }
    }

    protected UserRepresentation getUser(String username) {
        List<UserRepresentation> users = testRealm().users().search(username);
        assertFalse(users.isEmpty());
        return testRealm().users().get(users.get(0).getId()).toRepresentation();
    }

    protected UserRepresentation register() {
        navigateToRegistrationPage();
        String username = "jdoe";
        registerPage.register("firstName", "lastName", username + "@keycloak.org", username, "password", "password", CUSTOM_ATTRIBUTES);
        UserRepresentation user = getUser(username);
        getCleanup().addUserId(user.getId());
        return user;
    }

    private void navigateToRegistrationPage() {
        RealmRepresentation realm = testRealm().toRepresentation();
        realm.setRegistrationAllowed(true);
        testRealm().update(realm);
        loginPage.open();
        loginPage.clickRegister();
    }
}
