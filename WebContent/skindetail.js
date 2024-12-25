// JavaScript for Skin Details Page

// Function to fetch skin details based on the provided skin name
async function fetchSkinDetails() {
    const urlParams = new URLSearchParams(window.location.search);
    const skinName = urlParams.get('skinName');

    if (!skinName) {
        console.error("Missing skinName parameter.");
        return;
    }

    try {
        const response = await fetch(`/api/skin?skinName=${encodeURIComponent(skinName)}`);

        if (!response.ok) {
            throw new Error("Failed to fetch skin details.");
        }

        const skin = await response.json();
        console.log(skin);
        // Render skin details...
    } catch (error) {
        console.error(error);
    }
}


// Initialize the page by fetching the skin details
document.addEventListener('DOMContentLoaded', fetchSkinDetails);
