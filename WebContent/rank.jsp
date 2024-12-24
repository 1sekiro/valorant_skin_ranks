<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="common.css">
    <title>Skin Ranks</title>
</head>
<body>
<header>
    <div id="nav-buttons">
        <button id="vote-button" class="nav-btn">Vote</button>
        <button id="rank-button" class="nav-btn">Ranks</button>
    </div>
</header>
<div class="container mt-5">
    <h1 class="text-center">Skin Ranks</h1>

    <!-- Add filter button container -->
    <div class="filter-container">
        <button id="filter-button" class="filter-btn">Filter by Weapon</button>
    </div>

    <!-- Add weapon filter modal -->
    <div id="weapon-modal" class="modal">
        <div class="modal-content">
            <span class="close">&times;</span>
            <h2>Select Weapon</h2>
            <div id="weapon-grid"></div>
        </div>
    </div>

    <table id="skin_table" class="table table-striped table-bordered">
        <thead class="thead-dark">
        <tr>
            <th>Rank</th>
            <th>Icon</th>
            <th>Skin Name</th>
            <th>Wins</th>
        </tr>
        </thead>
        <tbody id="skin_table_body">
        <!-- Table rows will be populated here by JavaScript -->
        </tbody>
    </table>
</div>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="rank.js"></script>
</body>
</html>