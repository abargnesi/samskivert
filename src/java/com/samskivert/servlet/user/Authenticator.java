//
// $Id: Authenticator.java,v 1.1 2002/05/02 19:10:34 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2002 Walter Korman
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.servlet.user;

/**
 * Provides a means for applications to use their own application-specific
 * authentication schemes for validating a user by constructing their own
 * authenticator and passing it to {@link UserManager#login}.
 */
public interface Authenticator
{
    /**
     * Checks whether the user should be authenticated based on the
     * supplied user record and the specified user information.  Throws an
     * {@link AuthenticationFailedException} if the user fails to pass the
     * authentication check for some reason.
     *
     * @param user the definitive user record loaded from the persistent
     * repository against which the user-supplied data is to be checked.
     * @param username the username supplied by the user.
     * @param password the plaintext password supplied by the user.
     * @param persist if true, the cookie will expire in one month, if
     * false, the cookie will expire in 24 hours.
     *
     * @throws AuthenticationFailedException if the user failed to pass
     * the authentication check.
     */
    public void authenticateUser (
        User user, String username, Password password, boolean persist)
        throws AuthenticationFailedException;
}