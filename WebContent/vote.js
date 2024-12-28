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
        // Show loading overlay, hide only skins (not VS)
        $(".loading-overlay").show();
        $(".skin-clickable").hide();

        // VS container stays visible

        let url = "/valorant-skin-ranks/api/vote";
        if (weapon) {
            url += `?weapon=${weapon}`;
        }

        $.ajax({
            url: url,
            method: "GET",
            success: function (data) {
                if (data.skin1 && data.skin2) {
                    // Preload both images
                    const img1 = new Image();
                    const img2 = new Image();
                    let loadedImages = 0;

                    function showContent() {
                        $("#skin1-icon").attr("src", data.skin1.icon);
                        $("#skin1-name").text(data.skin1.name);
                        $("#vote-skin1").attr("data-skin-id", data.skin1.id);
                        $(".skin-clickable[data-vote-id='1']").attr("data-skin-id", data.skin1.id);

                        $("#skin2-icon").attr("src", data.skin2.icon);
                        $("#skin2-name").text(data.skin2.name);
                        $("#vote-skin2").attr("data-skin-id", data.skin2.id);
                        $(".skin-clickable[data-vote-id='2']").attr("data-skin-id", data.skin2.id);

                        // Hide loading, show skins
                        $(".loading-overlay").hide();
                        $(".skin-clickable").fadeIn(300);
                    }

                    // Set up image load handlers
                    [img1, img2].forEach(img => {
                        img.onload = () => {
                            loadedImages++;
                            if (loadedImages === 2) {
                                showContent();
                            }
                        };
                        img.onerror = () => {
                            loadedImages++;
                            if (loadedImages === 2) {
                                showContent();
                            }
                        };
                    });

                    // Start loading images
                    img1.src = data.skin1.icon;
                    img2.src = data.skin2.icon;
                }
            },
            error: function (error) {
                console.error("Failed to fetch skins:", error);
                $(".loading-overlay").hide();
                $(".skin-clickable").show();
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