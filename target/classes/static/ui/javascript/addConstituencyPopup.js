document.addEventListener("DOMContentLoaded", function () {
    const addConstituencyButton = document.getElementById("addConstituencyButton");
    const popup = document.getElementById("addConstituencyPopup");
    const closeButton = popup.querySelector(".close-button");

    addConstituencyButton.addEventListener("click", function () {
        popup.style.display = "block";
    });

    closeButton.addEventListener("click", function () {
        popup.style.display = "none";
    });
});
