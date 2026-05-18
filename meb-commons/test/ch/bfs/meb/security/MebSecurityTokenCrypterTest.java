package ch.bfs.meb.security;

import static ch.bfs.meb.util.SecurityConstants.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.extern.slf4j.Slf4j;

/** Test for {@link MebSecurityTokenCrypter}. */
@Slf4j
public class MebSecurityTokenCrypterTest {

    private static final String ENCRYPTED_TOKEN = "VNK2gpayQRkazxej9OJ43Og2P5SkIcxRfEUsA8VwFLv9OV2S6M5yemF1/90hgL92zWb8ASkdRZI6ZuuY/0JJzmzsjDzNaIL3/YCZ5kMGi+uy3q4pYEcalCsAMzA+m3egS0exy0yo7vrJ0KGcwMy6aVMe1sbe2I5CPll800LjJGu+xFfKjNZ3ZRH1PZfsvJzvhYH4NboplQrGIRwG5z4guL6PxifYIvk544/nYdjejtpO/Ii1fBjbC0pIkzAYtS5an2cxnZDotXE8g6WFuQcvJzVv4mxbfV9bygmM6Y9msMR5sFHSz2oBimdJLAhKaoEOK5hwrlRmwQJbC5l3HB3QxXnl0WPH4e4sLNG97yPU+dHeKmAzEL/5H7CMYbPtVUa3u8mTM3WkbHaQSAfJHWvjfLW5NPiVh4WaTcDPKxusEa5v+S8VwBKjWX2ibsR/zxlUAtG/jWOMoOok7k1VKt9GGDp60Zs/lZlaOnsW5xP1c/fBir1gLJ0a6VW3JA/f1BHvNG4bGtTVnOMZq1ZQvhGyizanZcIRE6i4Xn3Qrd5NdKHxRfyzWok+nYrooHJWG8Acp7NRpaRC1Tibqd2H9+Xq5xn1jv3DKiX/oGSQMx4dzTUY1KqbvfcKtZp/E4EbkYRW5GiWMomLFPIlrsEHeq8HZ/YB/2clRLtp+6JrD7rQTGT2w5Myeyhjo0korC49LOMVdB89kKbiNQJEakJJh990mhNmtUDTarxxhzQiO5Q8hdcFcBg0916V/RLO/j2TsAFbgCC/UU10A7HvUrExQIRWAPJUuCfdxdlHnU5PlqFIPPGHXHKpEMCklSiXoWuMn7S3TGNC97qmy+Yl5juUEaOgeMH54TAZoAurlv8mWzy1jAvZeP3kPRSXji3IhHhvK58LeXYY90RjOM40VxosSSEI1cE+xBBO9zdCo4QbbL/suNQubTJqJfckG9cM9JnEtHIl/YK6w+9RqMYcn0BfMLh/j0KkaoDd50iQyak4B9py0PWrrj09YjtBsxb999tdBJKU4yb2bqlF9Qi8Fn/i8m8XA3bY+sbMeq5xep8pcrMGLe4SRKqMIdN05Aqa9nYCQ+Q1";

    /** Test token as documented in the MEB Installationshandbuch" */
    private static final String DOCUMENTED_TOKEN = "VNK2gpayQRlq9b31AsIhna/MM58IIeQgbLrbd9ODIdfGPOXp2Srf8wou+c7A4UwBQegMMIAkvPm6cCxHHxboxblhpQ6q1xkCYX59pyz5FjOYnJaKTlLHx6FK4eymEpIy+2KQsninBrHUOS0mTbF1vxRh7+6WIMnsuCLKSF3dGQvNVQe20Qsld1XwFN23UTzEow9nkZzGqqc8Eg/EcSQmdw==";

    @Test
    public void encrypt() throws Exception {
        assertEquals(ENCRYPTED_TOKEN, MebSecurityTokenCrypter.encrypt("anonymous", "anonymous@nirgendwo.ch", getAllAuthorities()));
    }

    @Test
    public void decrypt() throws EncryptionException {
        Encrypter encrypter = new Encrypter(MebTokenConstants.KEYPHRASE);
        String decryptedToken = encrypter.decrypt(
                "Qb3VlqelEEB3kN+UdpK8BbnhoxTHOXgkMPOee4F7J8eaeRcx2skUxsEOXSjlTqrnjtj39jtDc/JArEMfe1BKKxPq/WMdkE9ZLTmN9IvLbp7vlxSE97xK/YPmdQpT+3ydtPmV4LGRJeKRAcf/yXDsdzT5D30q4EGvh7k6Ot8fMwHCKaWlKDqp465YBc76gI6x");
        assertEquals("CH6119869,Christine.AmmannTschopp@bfs.admin.ch,BFS-MEB.MEB_RO,BFS-MEB.SSP_DL,BFS-MEB.SSP_DV,BFS-MEB.SSP_EA,BFS-MEB.SSP_EV,BFS-MEB.SSP_RO", decryptedToken);
    }

    @Test
    public void ensureDocumentedTokenIsSame() {
        List<String> roleNames = Arrays.asList(ROLE_MEB_RO, ROLE_SBA_RO, ROLE_SBA_DL, ROLE_SBG_RO, ROLE_SBG_DL, ROLE_SDL_RO, ROLE_SDL_DL, ROLE_SSP_RO,
                ROLE_SSP_DL);
        List<GrantedAuthority> authorities = new ArrayList<>();
        roleNames.forEach(roleName -> authorities.add(new SimpleGrantedAuthority(roleName)));
        assertEquals(DOCUMENTED_TOKEN, MebSecurityTokenCrypter.encrypt("anonymous", "bfs@adesso.ch", authorities));
    }

    @Test
    public void getPrincipal() {
        assertEquals("anonymous", MebSecurityTokenCrypter.getPrincipal(ENCRYPTED_TOKEN));
    }

    @Test
    public void getEmail() {
        assertEquals("anonymous@nirgendwo.ch", MebSecurityTokenCrypter.getEmail(ENCRYPTED_TOKEN));
    }

    @Test
    public void getUserRoles() {
        List<GrantedAuthority> userTokens = MebSecurityTokenCrypter.getUserRoles(ENCRYPTED_TOKEN);
        assertEquals(new SimpleGrantedAuthority("BFS-MEB.MEB_RO"), userTokens.get(0));
        assertEquals(new SimpleGrantedAuthority("BFS-MEB.SBA_RO"), userTokens.get(1));
        assertEquals(new SimpleGrantedAuthority("BFS-MEB.SBA_DL"), userTokens.get(2));
        assertEquals("Unexpected group length.", 47, userTokens.size());
    }

    private List<GrantedAuthority> getAllAuthorities() {
        Collection<String> authoritiesAsStringList = new ArrayList<>();
        authoritiesAsStringList.add(ROLE_MEB_RO);
        authoritiesAsStringList.addAll(Arrays.asList(SBA_ROLE_HIERARCHY));
        authoritiesAsStringList.addAll(Arrays.asList(SBG_ROLE_HIERARCHY));
        authoritiesAsStringList.addAll(Arrays.asList(SDL_ROLE_HIERARCHY));
        authoritiesAsStringList.addAll(Arrays.asList(SSP_ROLE_HIERARCHY));
        authoritiesAsStringList.addAll(Arrays.asList(ROLES_CANTONS));
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authoritiesAsStringList.forEach(authorityAsString -> authorityList.add(new SimpleGrantedAuthority(authorityAsString)));
        return authorityList;
    }
}