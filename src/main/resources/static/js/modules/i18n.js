i18next
    .use(i18nextHttpBackend)
    .use(i18nextBrowserLanguageDetector)
    .init({
        lng: new URL(window.location.href).searchParams.get('locale') || 'en',
        fallbackLng: 'en',
        backend: {
            loadPath: '/locales/{{lng}}.json'
        }
    });

export default i18next;


