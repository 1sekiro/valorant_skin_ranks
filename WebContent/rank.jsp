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
        <button id="logo-button" class="nav-btn logo-btn">
            <span class="va-text">VA</span> SkinRanks
        </button>
        <div class="nav-right">
            <button id="vote-button" class="nav-btn">Vote</button>
            <button id="rank-button" class="nav-btn">Ranks</button>
        </div>
    </div>
</header>
<div class="container mt-5">
    <h1 class="text-center">Skin Ranks</h1>

    <!-- Add filter button container -->
    <div class="filter-container">
        <button id="filter-button" class="filter-btn">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="filter-icon">
                <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
            </svg>
            Filter
        </button>
    </div>

    <!-- Add weapon filter modal -->
    <div id="weapon-modal" class="modal">
        <div class="modal-content">
            <span class="close">&times;</span>
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
        </tbody>
    </table>
</div>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="rank.js"></script>
</body>
</html>