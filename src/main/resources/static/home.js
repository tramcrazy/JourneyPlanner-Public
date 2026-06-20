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

function updateOriginResults() {
    removeStationId("input-origin");
    let request = new XMLHttpRequest();
    request.addEventListener("load", insertOriginList);
    request.open("GET", `/api/search?query=${encodeURIComponent(inputOrigin.value)}`);
    request.send();
}

function insertOriginList() {
    if (this.status !== 200) {
        console.error("Unable to fetch search results for origin station - got unexpected HTTP response code from server.");
        appendAlert("Unable to fetch search results for origin station - unexpected response from server.", "danger");
    } else {
        const divOriginResults = document.getElementById("div-origin-results-placeholder");
        divOriginResults.innerHTML = this.responseText;
        setupResultsListeners("input-origin");
    }
}

function updateDestinationResults() {
    removeStationId("input-destination");
    let request = new XMLHttpRequest();
    request.addEventListener("load", insertDestinationList);
    request.open("GET", `/api/search?query=${encodeURIComponent(inputDestination.value)}`);
    request.send();
}

function insertDestinationList() {
    if (this.status !== 200) {
        console.error("Unable to fetch search results for origin station - got unexpected HTTP response code from server.");
        appendAlert("Unable to fetch search results for origin station - unexpected response from server.", "danger");
    } else {
        const divDestinationResults = document.getElementById("div-destination-results-placeholder");
        divDestinationResults.innerHTML = this.responseText;
        setupResultsListeners("input-destination");
    }
}

function setupResultsListeners(targetBox) {
    const resultsList = document.getElementsByClassName("button-search-result");
    for (let i = 0; i < resultsList.length; i++) {
        resultsList[i].addEventListener("click", fillInputBox);
        resultsList[i].targetBox = targetBox;
    }
}

function fillInputBox(event) {
    const inputBox = document.getElementById(event.currentTarget.targetBox);
    inputBox.dataset.stationId = event.currentTarget.dataset.stationId;
    inputBox.value = event.currentTarget.dataset.stationName;
    let currentList = event.currentTarget.parentNode;
    currentList.parentNode.removeChild(currentList);
}

function removeStationId(targetId) {
    const target = document.getElementById(targetId);
    delete target.dataset.stationId;
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

function switchDirection() {
    let originStationId = inputOrigin.dataset.stationId;
    let originStationName = inputOrigin.value;
    inputOrigin.dataset.stationId = inputDestination.dataset.stationId;
    inputOrigin.value = inputDestination.value;
    inputDestination.dataset.stationId = originStationId;
    inputDestination.value = originStationName;
}

function submitRequest() {
    const originStationId = inputOrigin.dataset.stationId;
    const destinationStationId = inputDestination.dataset.stationId;
    const divGreyout = document.getElementById("div-greyout");
    const divSpinner = document.getElementById("div-spinner");
    divGreyout.removeAttribute("hidden");
    divSpinner.removeAttribute("hidden");
    if (originStationId && destinationStationId) {
        location = `/result?origin=${originStationId}&destination=${destinationStationId}`;
    }
}

const anchorDisruptionUpdate = document.getElementById("anchor-disruption-update");
anchorDisruptionUpdate.addEventListener("click", updateDisruptions);

const inputOrigin = document.getElementById("input-origin");
inputOrigin.addEventListener("input", updateOriginResults);

const inputDestination = document.getElementById("input-destination");
inputDestination.addEventListener("input", updateDestinationResults);

const buttonSwitchDirection = document.getElementById("button-switch-direction");
buttonSwitchDirection.addEventListener("click", switchDirection);

const buttonSubmit = document.getElementById("button-submit");
buttonSubmit.addEventListener("click", submitRequest);

const darkModeStatus = matchMedia && matchMedia("(prefers-color-scheme: dark)");
if (darkModeStatus && darkModeStatus.matches) {
    document.documentElement.dataset.bsTheme = "dark";
}

window.onpageshow = function (event) {
    if (event.persisted) {
        location.reload();
    }
}