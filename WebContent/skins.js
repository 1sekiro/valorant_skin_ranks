$(document).ready(() => {
    // Fetch skin data from the servlet
    $.ajax({
        url: "/valorant-skin-ranks/api/skins",
        method: "GET",
        success: function (data) {
            const tableBody = $("#skin_table_body");
            tableBody.empty(); // Clear any existing rows

            data.forEach((skin) => {
                const row = `
                    <tr>
                        <td><img src="${skin.icon}" alt="${skin.skin_name}" style="width:100px; height:auto;"></td>
                        <td>${skin.skin_name}</td>
                        <td>${skin.price}</td>
                        <td>${skin.vote_count}</td>
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
