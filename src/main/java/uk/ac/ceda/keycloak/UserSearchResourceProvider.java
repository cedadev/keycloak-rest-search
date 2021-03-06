/**
 * Copyright 2021 United Kingdom Research and Innovation
 *
 * Authors: William Tucker
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ceda.keycloak;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager.AuthResult;
import org.keycloak.services.resource.RealmResourceProvider;

public class UserSearchResourceProvider implements RealmResourceProvider
{

    private KeycloakSession session;
    private final AuthResult auth;

    public UserSearchResourceProvider(KeycloakSession session)
    {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    public Object getResource()
    {
        return this;
    }

    @GET
    @Path("attribute/{attributeName}/{attributeValue}")
    @Produces(
    { MediaType.APPLICATION_JSON })
    public List<UserRepresentation> getUsersByAttribute(@PathParam("attributeName") String attributeName,
            @PathParam("attributeValue") String attributeValue)
    {
        if (this.auth == null || this.auth.getToken() == null)
        {
            throw new NotAuthorizedException("Bearer");
        }

        return session.users()
                .searchForUserByUserAttributeStream(session.getContext().getRealm(), attributeName, attributeValue)
                .map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(),
                        userModel))
                .collect(Collectors.toList());
    }

    @GET
    @Path("group/{groupId}")
    @Produces(
    { MediaType.APPLICATION_JSON })
    public List<UserRepresentation> getUsersByGroup(@PathParam("groupId") String groupId)
    {
        if (this.auth == null || this.auth.getToken() == null)
        {
            throw new NotAuthorizedException("Bearer");
        }

        GroupModel group = session.groups().getGroupById(session.getContext().getRealm(), groupId);

        return session.users()
                .getGroupMembersStream(session.getContext().getRealm(), group)
                .map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(),
                        userModel))
                .collect(Collectors.toList());
    }

    @GET
    @Path("query/{searchQuery}")
    @Produces(
    { MediaType.APPLICATION_JSON })
    public List<UserRepresentation> getUsersByQuery(@PathParam("searchQuery") String searchQuery)
    {
        if (this.auth == null || this.auth.getToken() == null)
        {
            throw new NotAuthorizedException("Bearer");
        }

        return session.users()
                .searchForUserStream(session.getContext().getRealm(), searchQuery)
                .map(userModel -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(),
                        userModel))
                .collect(Collectors.toList());
    }

    public void close()
    {
    }

}
