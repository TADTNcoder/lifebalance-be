<#import "field.ftl" as field>

<#macro username>
  <#assign label>
    <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
  </#assign>
  <@field.group name="username" label=label>
    <div class="${properties.kcInputGroup}">
      <div class="${properties.kcInputGroupItemClass} ${properties.kcFill}">
        <span class="${properties.kcInputClass} ${properties.kcFormReadOnlyClass}">
          <input id="kc-attempted-username" value="${auth.attemptedUsername}" readonly>
        </span>
      </div>
      <div class="${properties.kcInputGroupItemClass}">
        <button id="reset-login" class="${properties.kcFormPasswordVisibilityButtonClass} kc-login-tooltip" type="button"
              aria-label="${msg('restartLoginTooltip')}" onclick="location.href='${url.loginRestartFlowUrl}'">
            <i class="fa-sync-alt fas" aria-hidden="true"></i>
            <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
        </button>
      </div>
    </div>
  </@field.group>
</#macro>

<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" lang="${lang}"<#if realm.internationalizationEnabled> dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="color-scheme" content="dark">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}" />
        </#list>
    </#if>

    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />

    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>

    <script type="importmap">
        {
            "imports": {
                "rfc4648": "${url.resourcesCommonPath}/vendor/rfc4648/rfc4648.js"
            }
        }
    </script>

    <#if darkMode>
      <script type="module" async blocking="render">
          const DARK_MODE_CLASS = "${properties.kcDarkModeClass}";
          const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
          updateDarkMode(mediaQuery.matches);
          mediaQuery.addEventListener("change", (event) => updateDarkMode(event.matches));
          function updateDarkMode(isEnabled) {
            const { classList } = document.documentElement;
            if (isEnabled) classList.add(DARK_MODE_CLASS);
            else classList.remove(DARK_MODE_CLASS);
          }
      </script>
    </#if>

    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>

    <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
    <script type="module">
        import { startSessionPolling } from "${url.resourcesPath}/js/authChecker.js";
        startSessionPolling("${url.ssoLoginInOtherTabsUrl?no_esc}");
    </script>
    <#if authenticationSession??>
        <script type="module">
            import { checkAuthSession } from "${url.resourcesPath}/js/authChecker.js";
            checkAuthSession("${authenticationSession.authSessionIdHash}");
        </script>
    </#if>
</head>

<body id="keycloak-bg" class="${properties.kcBodyClass!}<#if bodyClass?has_content> ${bodyClass}</#if>">
    <#assign appHomeUrl = (properties.appHomeUrl!'http://localhost:5173/')>
    <#if client?? && client.clientId?? && client.clientId == "lifebalance-web">
        <#if client.baseUrl?? && client.baseUrl?has_content && (client.baseUrl?starts_with("http://") || client.baseUrl?starts_with("https://"))>
            <#assign appHomeUrl = client.baseUrl>
        <#elseif client.rootUrl?? && client.rootUrl?has_content && (client.rootUrl?starts_with("http://") || client.rootUrl?starts_with("https://"))>
            <#assign appHomeUrl = client.rootUrl>
        </#if>
    </#if>
    <div class="auth-shell">
        <header class="auth-topbar" aria-label="LifeBalance authentication">
            <a class="auth-brand" href="${appHomeUrl}" aria-label="LifeBalance home">
                <span class="auth-brand-mark" aria-hidden="true">LB</span>
                <span class="auth-brand-text">LifeBalance</span>
            </a>

            <nav class="auth-nav" aria-label="Authentication links">
                <a href="${appHomeUrl}">Home</a>
                <a href="${appHomeUrl}">Help Center</a>
                <#if realm.internationalizationEnabled && locale.supported?size gt 1>
                    <label class="auth-locale">
                        <span class="auth-sr-only">${msg("languages")}</span>
                        <select
                            aria-label="${msg("languages")}"
                            onchange="if (this.value) window.location.href=this.value"
                        >
                            <#list locale.supported?sort_by("label") as l>
                                <option value="${l.url}" ${(l.languageTag == locale.currentLanguageTag)?then('selected','')}>
                                    ${l.label}
                                </option>
                            </#list>
                        </select>
                    </label>
                </#if>
                <#if realm.registrationAllowed && !registrationDisabled??>
                    <a class="auth-nav-primary" href="${url.registrationUrl}">${msg("doRegister")}</a>
                </#if>
            </nav>
        </header>

        <main class="auth-main">
            <section class="auth-identity" aria-labelledby="auth-identity-title">
                <div class="auth-identity-content">
                    <span class="auth-eyebrow">Identity Gateway</span>
                    <h2 id="auth-identity-title">LifeBalance Identity</h2>
                    <p>Secure access for your LifeBalance workspace.</p>
                    <ul class="auth-assurance-list" aria-label="Authentication assurances">
                        <li>
                            <span class="auth-check" aria-hidden="true"></span>
                            <span>OpenID Connect</span>
                        </li>
                        <li>
                            <span class="auth-check" aria-hidden="true"></span>
                            <span>Role-based access</span>
                        </li>
                        <li>
                            <span class="auth-check" aria-hidden="true"></span>
                            <span>Session protection</span>
                        </li>
                    </ul>
                </div>
            </section>

            <section class="auth-panel" aria-labelledby="kc-page-title">
                <div class="auth-panel-header">
                    <span class="auth-eyebrow">LifeBalance</span>
                    <h1 id="kc-page-title"><#nested "header"></h1>
                    <#if displayRequiredFields>
                        <p class="auth-required">* ${msg("requiredFields")}</p>
                    </#if>
                </div>

                <#if auth?has_content && auth.showUsername() && !auth.showResetCredentials()>
                    <div class="auth-attempted-user">
                        <#nested "show-username">
                        <@username />
                    </div>
                </#if>

                <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                    <div class="${properties.kcAlertClass!} pf-m-${(message.type = 'error')?then('danger', message.type)}" role="alert" aria-live="polite">
                        <div class="${properties.kcAlertIconClass!}">
                            <#if message.type = 'success'>
                                <span class="${properties.kcFeedbackSuccessIcon!}"></span>
                            </#if>
                            <#if message.type = 'warning'>
                                <span class="${properties.kcFeedbackWarningIcon!}"></span>
                            </#if>
                            <#if message.type = 'error'>
                                <span class="${properties.kcFeedbackErrorIcon!}"></span>
                            </#if>
                            <#if message.type = 'info'>
                                <span class="${properties.kcFeedbackInfoIcon!}"></span>
                            </#if>
                        </div>
                        <span class="${properties.kcAlertTitleClass!}">
                            ${kcSanitize(message.summary)?no_esc}
                        </span>
                    </div>
                </#if>

                <div class="auth-panel-body">
                    <#nested "form">
                </div>

                <#if auth?has_content && auth.showTryAnotherWayLink()>
                    <form id="kc-select-try-another-way-form" class="auth-try-another" action="${url.loginAction}" method="post" novalidate="novalidate">
                        <input type="hidden" name="tryAnotherWay" value="on"/>
                        <button id="try-another-way" type="submit" class="${properties.kcButtonSecondaryClass} ${properties.kcButtonBlockClass}">
                            ${kcSanitize(msg("doTryAnotherWay"))?no_esc}
                        </button>
                    </form>
                </#if>

                <#if social.providers?? && social.providers?has_content>
                    <div class="auth-social">
                        <#nested "socialProviders">
                    </div>
                </#if>

                <#if displayInfo>
                    <div id="kc-info" class="auth-info">
                        <div id="kc-info-wrapper">
                            <#nested "info">
                        </div>
                    </div>
                </#if>
            </section>
        </main>

        <footer class="auth-footer">
            <span>Copyright 2026 LifeBalance. All rights reserved.</span>
            <nav aria-label="Footer links">
                <a href="${appHomeUrl}">Privacy Policy</a>
                <a href="${appHomeUrl}">Help Center</a>
                <a href="${appHomeUrl}">Terms of Service</a>
            </nav>
        </footer>
    </div>
</body>
</html>
</#macro>
