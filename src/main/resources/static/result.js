function insertDisruptionHtml() {
    if (this.status !== 200) {
        console.error("Unable to update disruption data - got unexpected HTTP response code from server.");
        appendAlert("Unable to fetch disruption data - unexpected response from server.", "danger");
    } else {
        const spanDisruptionDots = document.getElementById("span-disruption-dots");
        spanDisruptionDots.innerHTML = this.response.dots;
        const spanDisruptionDescription = document.getElementById("span-disruption-description");
        spanDisruptionDescription.innerHTML = this.response.descriptions;
        appendAlert("Disruption data updated.", "success");
    }
    anchorDisruptionUpdate.classList.remove("anchor-disabled");
}

function updateDisruptions() {
    anchorDisruptionUpdate.classList.add("anchor-disabled");
    let request = new XMLHttpRequest();
    request.responseType = "json";
    request.addEventListener("load", insertDisruptionHtml);
    request.open("GET", "/api/disruptions");
    request.send();
}

const alertPlaceholder = document.getElementById('div-alert-placeholder');
function appendAlert(message, type) {
    const wrapper = document.createElement('div');
    wrapper.innerHTML = [
        `<div class="alert alert-${type} alert-dismissible mt-3" role="alert">`,
        `   <div>${message}</div>`,
        '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
        '</div>'
    ].join('');

    alertPlaceholder.append(wrapper);
}

function recalculateJourney() {
    const divGreyout = document.getElementById("div-greyout");
    const divSpinner = document.getElementById("div-spinner");
    divGreyout.removeAttribute("hidden");
    divSpinner.removeAttribute("hidden");
    location.reload();
}

function switchResultDirection() {
    const divGreyout = document.getElementById("div-greyout");
    const divSpinner = document.getElementById("div-spinner");
    divGreyout.removeAttribute("hidden");
    divSpinner.removeAttribute("hidden");
    let queryParams = new URLSearchParams(document.location.search);
    let origin = queryParams.get("origin");
    let destination = queryParams.get("destination");
    queryParams.set("origin", destination);
    queryParams.set("destination", origin);
    location.search = queryParams.toString();
}

const darkModeStatus = matchMedia && matchMedia("(prefers-color-scheme: dark)");
if (darkModeStatus && darkModeStatus.matches) {
    document.documentElement.dataset.bsTheme = "dark";
}

const anchorDisruptionUpdate = document.getElementById("anchor-disruption-update");
anchorDisruptionUpdate.addEventListener("click", updateDisruptions);

const buttonRecalculate = document.getElementById("button-recalculate");
buttonRecalculate.addEventListener("click", recalculateJourney);

const buttonSwitchDirection = document.getElementById("button-switch-direction");
buttonSwitchDirection.addEventListener("click", switchResultDirection)