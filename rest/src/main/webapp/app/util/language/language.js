angular.module('tsApp').controller('LangControls', LangControls);
function LangControls($translate ) {
    var vm = this;
    vm.changeLanguage = changeLanguage;
    vm.currentLang = $translate.use();
    function changeLanguage(langKey) {
        $translate.use(langKey);
        vm.currentLang = $translate.use();
    }
}