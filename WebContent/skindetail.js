document.addEventListener('DOMContentLoaded', () => {
    // Fetch skin details
    fetchSkinDetails();

    // Navigation button handlers
    document.getElementById('logo-button').addEventListener('click', () => {
        window.location.href = "/valorant-skin-ranks/vote.html";
    });

    document.getElementById('vote-button').addEventListener('click', () => {
        window.location.href = "/valorant-skin-ranks/vote.html";
    });

    document.getElementById('rank-button').addEventListener('click', () => {
        window.location.href = "/valorant-skin-ranks/rank.jsp";
    });
});

async function fetchSkinDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const skinName = urlParams.get('skinName');

    if (!skinName) {
        console.error("Missing skinName parameter in URL");
        document.getElementById('skin-details').innerHTML = '<p class="error">No skin name provided.</p>';
        return;
    }

    try {
        console.log('Fetching data for skin:', skinName);

        // Fetch skin details
        console.log('Fetching skin details...');
        const skinUrl = `/valorant-skin-ranks/api/skin?skinName=${encodeURIComponent(skinName)}`;
        console.log('Skin URL:', skinUrl);
        const skinResponse = await fetch(skinUrl);
        console.log('Skin response status:', skinResponse.status);

        // Fetch vote history
        console.log('Fetching vote history...');
        const historyUrl = `/valorant-skin-ranks/api/vote-history?skinName=${encodeURIComponent(skinName)}`;
        console.log('History URL:', historyUrl);
        const historyResponse = await fetch(historyUrl);
        console.log('History response status:', historyResponse.status);

        if (!skinResponse.ok || !historyResponse.ok) {
            throw new Error(`Error fetching data: Skin status ${skinResponse.status}, History status ${historyResponse.status}`);
        }

        const skin = await skinResponse.json();
        console.log('Skin data:', skin);

        const history = await historyResponse.json();
        console.log('History data:', history);

        // Create the main skin details HTML
        let detailsHtml = `
            <div class="detail-header">
                <img class="skin-icon" src="${skin.icon}" alt="${skin.skin_name}" 
                     onerror="this.onerror=null; this.src='placeholder.png';">
                <div class="skin-info">
                    <h1 class="skin-name">${skin.skin_name}</h1>
                </div>
            </div>
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Wins</div>
                    <div class="stat-value">${skin.win_num}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Total Votes</div>
                    <div class="stat-value">${skin.vote_count}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Loss Count</div>
                    <div class="stat-value">${skin.vote_count - skin.win_num}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Win Rate</div>
                    <div class="stat-value win-rate">${skin.win_rate}%</div>
                </div>
            </div>`;

        // Add vote history section
        if (history.history && history.history.length > 0) {
            console.log(`Adding ${history.history.length} match history items`);
            detailsHtml += `
                <div class="history-section">
                    <h2>Recent Matches</h2>
                    <div class="match-history">`;

            history.history.forEach((match, index) => {
                console.log(`Processing match ${index}:`, match);

                // Ensure win_rate is a valid number, default to 0 if not
                const winRate = Number.isFinite(match.win_rate) ? match.win_rate : 0;
                const inverseWinRate = Number.isFinite(match.win_rate) ? (100 - match.win_rate) : 0;

                detailsHtml += `
                    <div class="match-card ${match.result}">
                        <div class="match-content">
                            <div class="match-skin">
                                <img src="${match.winner_icon}" alt="${match.winner_name}">
                                <span>${match.winner_name}</span>
                                <div class="match-stats">
                                    <span class="win-rate">${match.winner_name === skinName ? winRate.toFixed(1) : inverseWinRate.toFixed(1)}% Win Rate</span>
                                    <span class="total-matches">${match.total_matches} MATCHES</span>
                                </div>
                            </div>
                            <div class="vs">VS</div>
                            <div class="match-skin">
                                <img src="${match.loser_icon}" alt="${match.loser_name}">
                                <span>${match.loser_name}</span>
                                <div class="match-stats">
                                    <span class="win-rate">${match.loser_name === skinName ? winRate.toFixed(1) : inverseWinRate.toFixed(1)}% Win Rate</span>
                                    <span class="total-matches">${match.total_matches} MATCHES</span>
                                </div>
                            </div>
                        </div>
                        <div class="match-result ${match.result}">${match.result.toUpperCase()}</div>
                    </div>`;
            });

            detailsHtml += `
                    </div>
                </div>`;
        } else {
            console.log('No match history available');
            detailsHtml += `
                <div class="history-section">
                    <h2>Recent Matches</h2>
                    <p class="no-matches">No match history available</p>
                </div>`;
        }

        document.getElementById('skin-details').innerHTML = detailsHtml;
    } catch (error) {
        console.error('Error:', error);
        document.getElementById('skin-details').innerHTML = `
            <p class="error">Error loading skin details: ${error.message}</p>
            <button onclick="fetchSkinDetails()" class="retry-button">Retry</button>
        `;
    }
}