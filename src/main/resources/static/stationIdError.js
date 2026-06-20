const darkModeStatus = matchMedia && matchMedia("(prefers-color-scheme: dark)");
if (darkModeStatus && darkModeStatus.matches) {
    document.documentElement.dataset.bsTheme = "dark";
}