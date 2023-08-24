const popupDiv = document.getElementById("popup");
const form = document.querySelector("form");

function showPopupAndClearFields() {
    popupDiv.style.display = "block";
    setTimeout(function () {
        popupDiv.style.display = "none";
    }, 2000);

    // Clear the form fields
    form.reset();
    window.location.href = "/ui/add-user";

}

// Add an event listener for the form submission
form.addEventListener("submit", function (event) {
    event.preventDefault();

    // Submit the form using AJAX or default form submission
    // AJAX example:
    fetch(form.action, {
        method: "POST",
        body: new FormData(form),
    })
        .then(function (response) {
            if (response.ok) {
                showPopupAndClearFields();
            } else {
                // Handle error cases
            }
        })
        .catch(function (error) {
            // Handle error cases
        });
});

const userTypeSelect = document.getElementById('userType');
const constituencyDropdown = document.getElementById('constituencyDropdown');
const partyDropdown = document.getElementById('partyDropdown');

// Add event listener for user type change
userTypeSelect.addEventListener('change', function() {
    const selectedOption = userTypeSelect.value;

    // Show/hide the appropriate dropdown based on user type
    if (selectedOption === 'Candidate') {
        constituencyDropdown.style.display = 'block';
        partyDropdown.style.display = 'block';
    } else {
        constituencyDropdown.style.display = 'none';
        partyDropdown.style.display = 'none';
    }
});
