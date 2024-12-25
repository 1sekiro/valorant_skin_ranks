// JavaScript for Skin Details Page

// Function to fetch skin details based on the provided skin ID
async function fetchSkinDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const skinId = urlParams.get('skinId');

    const detailsContainer = document.getElementById('skin-details');

    if (!skinId) {
        detailsContainer.innerHTML = '<p class="error">No skin ID provided.</p>';
        return;
    }

    try {
        const response = await fetch(`/api/skin?skinId=${skinId}`);

        if (!response.ok) {
            throw new Error('Failed to fetch skin details.');
        }

        const skin = await response.json();

        if (skin.error) {
            detailsContainer.innerHTML = `<p class="error">${skin.error}</p>`;
            return;
        }

        detailsContainer.innerHTML = `
            <img class="skin-icon" src="${skin.icon}" alt="${skin.skin_name}">
            <h1>${skin.skin_name}</h1>
            <p><strong>Wins:</strong> ${skin.win_num}</p>
            <p><strong>Losses:</strong> ${skin.loss_num}</p>
            <p><strong>Win Rate:</strong> ${skin.win_rate}%</p>
        `;
    } catch (error) {
        detailsContainer.innerHTML = `<p class="error">${error.message}</p>`;
    }
}

// Initialize the page by fetching the skin details
document.addEventListener('DOMContentLoaded', fetchSkinDetails);