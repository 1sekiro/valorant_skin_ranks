$(document).ready(() => {
    let selectedWeapon = null;

    fetchRandomSkins();

    $("#vote-button").click(() => {
        window.location.href = "/valorant-skin-ranks/vote.html";
    });

    $("#rank-button").click(() => {
        window.location.href = "/valorant-skin-ranks/rank.jsp";
    });

    $("#logo-button").click(() => {
        window.location.href = "/valorant-skin-ranks/vote.html";
    });

    // Modal handling
    const modal = $("#weapon-modal");
    const filterBtn = $("#filter-button");
    const closeBtn = $(".close");

    filterBtn.click(() => {
        modal.css("display", "block");
        loadWeapons();
    });

    closeBtn.click(() => {
        modal.css("display", "none");
    });

    $(window).click((event) => {
        if ($(event.target).is(modal)) {
            modal.css("display", "none");
        }
    });

    function loadWeapons() {
        $.ajax({
            url: "/valorant-skin-ranks/api/weapons",
            method: "GET",
            success: function(weapons) {
                const weaponGrid = $("#weapon-grid");
                weaponGrid.empty();

                weapons.forEach((weapon) => {
                    const weaponElement = `
                        <div class="weapon-item" data-weapon-name="${weapon.weapon_name}">
                            <img src="${weapon.icon}" alt="${weapon.weapon_name}" class="weapon-icon">
                            <span class="weapon-name">${weapon.weapon_name}</span>
                        </div>
                    `;
                    weaponGrid.append(weaponElement);
                });
            }
        });
    }

    // Weapon selection handling
    $("#weapon-grid").on("click", ".weapon-item", function() {
        selectedWeapon = $(this).data("weapon-name");
        fetchRandomSkins(selectedWeapon);
        modal.css("display", "none");
    });

    function fetchRandomSkins(weapon = null) {
        let url = "/valorant-skin-ranks/api/vote";
        if (weapon) {
            url += `?weapon=${weapon}`;
        }

        $.ajax({
            url: url,
            method: "GET",
            success: function (data) {
                if (data.skin1 && data.skin2) {
                    $("#skin1-icon").attr("src", data.skin1.icon);
                    $("#skin1-name").text(data.skin1.name);
                    $("#vote-skin1").attr("data-skin-id", data.skin1.id);
                    // Add this line
                    $("#skin1-icon").closest(".skin-clickable").attr("data-skin-id", data.skin1.id);

                    $("#skin2-icon").attr("src", data.skin2.icon);
                    $("#skin2-name").text(data.skin2.name);
                    $("#vote-skin2").attr("data-skin-id", data.skin2.id);
                    // Add this line
                    $("#skin2-icon").closest(".skin-clickable").attr("data-skin-id", data.skin2.id);
                }
            },
            error: function (error) {
                console.error("Failed to fetch skins:", error);
            },
        });
    }

    $("#vote-skin1").click(function () {
        const winningSkinId = $(this).attr("data-skin-id");
        const losingSkinId = $("#vote-skin2").attr("data-skin-id");
        voteForSkin(winningSkinId, losingSkinId);
    });

    $("#vote-skin2").click(function () {
        const winningSkinId = $(this).attr("data-skin-id");
        const losingSkinId = $("#vote-skin1").attr("data-skin-id");
        voteForSkin(winningSkinId, losingSkinId);
    });

    function voteForSkin(winningSkinId, losingSkinId) {
        $.ajax({
            url: "/valorant-skin-ranks/api/vote",
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                skinId: winningSkinId,
                otherSkinId: losingSkinId
            }),
            success: function () {
                fetchRandomSkins(selectedWeapon);
            },
            error: function (error) {
                console.error("Failed to vote:", error);
            },
        });
    }

    $("#vote-container").on("click", ".skin-clickable", function() {
        const voteId = $(this).data("vote-id");
        if (voteId === 1) {
            const winningSkinId = $(this).attr("data-skin-id");
            const losingSkinId = $(".skin-clickable[data-vote-id='2']").attr("data-skin-id");
            voteForSkin(winningSkinId, losingSkinId);
        } else {
            const winningSkinId = $(this).attr("data-skin-id");
            const losingSkinId = $(".skin-clickable[data-vote-id='1']").attr("data-skin-id");
            voteForSkin(winningSkinId, losingSkinId);
        }
    });
});