$(document).ready(() => {
    $("#vote-button").click(() => {
        window.location.href = "/valorant-skin-ranks/vote.html";
    });

    $("#rank-button").click(() => {
        window.location.href = "/valorant-skin-ranks/rank.jsp";
    });

    // Fetch skin data from the servlet
    $.ajax({
        url: "/valorant-skin-ranks/api/rank",
        method: "GET",
        success: function (data) {
            const tableBody = $("#skin_table_body");
            tableBody.empty(); // Clear any existing rows

            data.forEach((skin) => {
                const row = `
                    <tr>
                        <td><img src="${skin.icon}" alt="${skin.skin_name}" style="width:100px; height:auto;"></td>
                        <td>${skin.skin_name}</td>
                        <td>${skin.win_num}</td>
                    </tr>
                `;
                tableBody.append(row);
            });
        },
        error: function (error) {
            console.error("Failed to fetch skins:", error);
        },
    });
});
