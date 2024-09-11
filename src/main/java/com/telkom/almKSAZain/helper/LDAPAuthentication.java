//package com.telkom.almKSAZain.helper;
//
//import javax.naming.CommunicationException;
//import javax.naming.Context;
//import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;
//import javax.naming.directory.Attribute;
//import javax.naming.directory.Attributes;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.InitialDirContext;
//import javax.security.auth.login.AccountException;
//import javax.security.auth.login.FailedLoginException;
//import javax.security.auth.login.LoginException;
//import javax.swing.JOptionPane;
//import java.util.ArrayList;
//import java.util.Hashtable;
//import java.util.List;
//
//public class LDAPAuthentication {
//    private static final String CONTEXT_FACTORY_CLASS = "com.sun.jndi.ldap.LdapCtxFactory";
//    private String[] ldapServerUrls;
//    private int lastLdapUrlIndex;
//    private final String domainName;
//
//    public LDAPAuthentication() {
//        this.domainName = "TELKOM.CO.KE";
//        try {
//            this.ldapServerUrls = nsLookup(domainName);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        this.lastLdapUrlIndex = 0;
//    }
//
//    public boolean authenticate(String username, String password) throws LoginException {
//        if (this.ldapServerUrls == null || this.ldapServerUrls.length == 0)
//            throw new AccountException("Unable to find ldap servers");
//        if (username == null || password == null || username.isBlank() || password.isBlank())
//            throw new FailedLoginException("Username or password is empty");
//        int retryCount = 0;
//        int currentLdapUrlIndex = this.lastLdapUrlIndex;
//        do {
//            retryCount++;
//            try {
//                Hashtable<Object, Object> env = new Hashtable<>();
//                env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
//                env.put(Context.PROVIDER_URL, this.ldapServerUrls[currentLdapUrlIndex]);
//                env.put(Context.SECURITY_PRINCIPAL, "%s@%s".formatted(username, this.domainName));
//                env.put(Context.SECURITY_CREDENTIALS, password);
//
//                DirContext ctx = new InitialDirContext(env);
//                ctx.close();
//
//                this.lastLdapUrlIndex = currentLdapUrlIndex;
//                return true;
//            } catch (CommunicationException exp) {
//                // TODO you can replace with log4j or slf4j API
//                exp.printStackTrace();
//                // if the exception of type communication we can assume the AD
//                // is not reachable hence retry can be attempted with next
//                // available AD
//                if (retryCount < this.ldapServerUrls.length) {
//                    currentLdapUrlIndex++;
//                    if (currentLdapUrlIndex == this.ldapServerUrls.length)
//                        currentLdapUrlIndex = 0;
//                    continue;
//                }
//                return false;
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//                return false;
//            }
//        } while (true);
//    }
//
//    private static String[] nsLookup(String argDomain) throws Exception {
//        try {
//            Hashtable<Object, Object> env = new Hashtable<>();
//            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
//            env.put("java.naming.provider.url", "dns:");
//            DirContext ctx = new InitialDirContext(env);
//            Attributes attributes = ctx.getAttributes(String.format("_ldap._tcp.%s", argDomain), new String[]{"srv"});
//            // try thrice to get the KDC servers before throwing error
//            for (int i = 0; i < 3; i++) {
//                Attribute a = attributes.get("srv");
//                if (a != null) {
//                    List<String> domainServers = new ArrayList<>();
//                    NamingEnumeration<?> enumeration = a.getAll();
//                    while (enumeration.hasMoreElements()) {
//                        String srvAttr = (String) enumeration.next();
//                        // the value are in space separated 0) priority 1)
//                        // weight 2) port 3) server
//                        String[] values = srvAttr.split(" ");
//                        domainServers.add(String.format("ldap://%s:%s", values[3], values[2]));
//                    }
//                    return domainServers.toArray(String[]::new);
//                }
//            }
//            throw new Exception("Unable to find srv attribute for the domain " + argDomain);
//        } catch (NamingException exp) {
//            throw new Exception("Error while performing nslookup. Root Cause: " + exp.getMessage(), exp);
//        }
//    }
//
//
//}
