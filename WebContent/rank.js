$(document).ready(() => {
    let currentWeaponName = null;

    $("#vote-button").click(() => {
        window.location.href = "/valorant-skin-ranks/vote.html";
    });

    $("#rank-button").click(() => {
        window.location.href = "/valorant-skin-ranks/rank.jsp";
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

    function loadSkins(weaponName = "vandal") {
        let url = "/valorant-skin-ranks/api/rank";
        if (weaponName) {
            url += `?weaponName=${weaponName}`;
        }

        $.ajax({
            url: url,
            method: "GET",
            success: function(data) {
                const tableBody = $("#skin_table_body");
                tableBody.empty();

                data.forEach((skin, index) => {
                    const row = `
                    <tr>
                        <td>${index + 1}</td>
                        <td class="skin-cell">
                            <img src="${skin.icon}" alt="${skin.skin_name}" class="rank-table-icon">
                        </td>
                        <td class="skin-name">${skin.skin_name}</td>
                        <td>${skin.win_num}</td>
                    </tr>
                `;
                    tableBody.append(row);
                });

                // Add click handlers after creating the rows
                $(".skin-cell").click(function() {
                    const skinName = $(this).closest('tr').find('.skin-name').text();
                    window.location.href = `/valorant-skin-ranks/skindetail.html?skinName=${encodeURIComponent(skinName)}`;
                });
            },
            error: function(error) {
                console.error("Failed to fetch skins:", error);
            }
        });
    }

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

    $("#weapon-grid").on("click", ".weapon-item", function() {
        currentWeaponName = $(this).data("weapon-name");
        loadSkins(currentWeaponName);
        modal.css("display", "none");
    });

    loadSkins("vandal");
});