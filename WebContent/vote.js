$(document).ready(() => {
    // Initially fetch random skins without a weapon filter
    fetchRandomSkins();

    // Handle the vote button click to refresh the page with new skins
    $("#vote-button").click(() => {
        fetchRandomSkins();
    });

    $("#rank-button").click(() => {
        window.location.href = "/valorant-skin-ranks/rank.jsp";
    });

    // Handle the filter button click to toggle the dropdown
    $("#filter-button").click(() => {
        $("#weapon-select").toggle();
    });

    // Handle weapon selection from the dropdown
    $("#weapon-select").change(function () {
        const selectedWeapon = $(this).val(); // Get the selected weapon
        fetchRandomSkins(selectedWeapon); // Fetch skins for the selected weapon
    });

    // Function to fetch skins from the backend
    function fetchRandomSkins(selectedWeapon = null) {
        let url = "/valorant-skin-ranks/api/vote";
        if (selectedWeapon) {
            url += `?weapon=${selectedWeapon}`;
        }

        $.ajax({
            url: url,
            method: "GET",
            success: function (data) {
                if (data.skin1 && data.skin2) {
                    // Update skin 1 details
                    $("#skin1-icon").attr("src", data.skin1.icon);
                    $("#skin1-name").text(data.skin1.name);
                    $("#vote-skin1").data("skin-id", data.skin1.id);

                    // Update skin 2 details
                    $("#skin2-icon").attr("src", data.skin2.icon);
                    $("#skin2-name").text(data.skin2.name);
                    $("#vote-skin2").data("skin-id", data.skin2.id);
                } else {
                    console.error("Invalid response format:", data);
                }
            },
            error: function (error) {
                console.error("Failed to fetch skins:", error);
            },
        });
    }

    // Handle voting for skin 1
    $("#vote-skin1").click(function () {
        const skinId = $(this).data("skin-id");
        voteForSkin(skinId);
    });

    // Handle voting for skin 2
    $("#vote-skin2").click(function () {
        const skinId = $(this).data("skin-id");
        voteForSkin(skinId);
    });

    // Function to send a vote to the backend
    function voteForSkin(skinId) {
        $.ajax({
            url: "/valorant-skin-ranks/api/vote",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({ skinId }),
            success: function () {
                fetchRandomSkins(); // Refresh skins after voting
            },
            error: function (error) {
                console.error("Failed to vote:", error);
            },
        });
    }
});
