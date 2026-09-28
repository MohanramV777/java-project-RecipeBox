const API = "/api";
let userId = null;
let recipes = [];
let currentFavorites = false;

const $ = id => document.getElementById(id);
const toast = msg => {
    $("toast").textContent = msg;
    $("toast").classList.add("toast-show");
    setTimeout(() => $("toast").classList.remove("toast-show"), 2200);
};

async function api(url, options = {}) {
    const response = await fetch(API + url, {
        headers: {"Content-Type": "application/json"},
        ...options
    });
    const text = await response.text();
    let data = {};
    try { data = text ? JSON.parse(text) : {}; } catch {}
    if (!response.ok) throw new Error(data.message || "Request failed");
    return data;
}

async function init() {
    try {
        const users = await api("/users");
        if (!users.length) {
            const created = await api("/users", {
                method: "POST",
                body: JSON.stringify({name:"Home Cook", email:"cook@recipebox.local"})
            });
            users.push(created);
        }
        $("userSelect").innerHTML = users.map(u => `<option value="${u.id}">${escapeHtml(u.name)}</option>`).join("");
        userId = users[0].id;
        $("weekDate").value = today();
        loadRecipes();
        loadWeek();
    } catch (e) { toast(e.message); }
}

function today() { return new Date().toISOString().slice(0,10); }
function escapeHtml(v) { return String(v ?? "").replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

async function loadRecipes() {
    try {
        const search = $("search").value.trim();
        const query = new URLSearchParams({userId, favorites: currentFavorites});
        if (search) query.set("search", search);
        recipes = await api("/recipes?" + query);
        renderRecipes();
        fillPlanRecipes();
    } catch(e) { toast(e.message); }
}

function renderRecipes() {
    $("recipeCount").textContent = `${recipes.length} recipe${recipes.length === 1 ? "" : "s"}`;
    if (!recipes.length) {
        $("recipeGrid").innerHTML = `<div class="empty">No recipes yet. Add your first one.</div>`;
        return;
    }
    $("recipeGrid").innerHTML = recipes.map(r => `
        <article class="recipe-card">
            <div>
                <div class="recipe-top">
                    <div>
                        <h4>${escapeHtml(r.title)}</h4>
                        <span class="cuisine">${escapeHtml(r.cuisine)}</span>
                    </div>
                    <button class="heart" onclick="toggleFavorite(${r.id})">${r.favorite ? "♥" : "♡"}</button>
                </div>
                <div class="meta"><span>⏱ ${r.prepMinutes} min</span><span>🍽 ${r.servings} servings</span><span>${r.ingredients.length} items</span></div>
            </div>
            <div class="card-actions">
    <div class="card-edit-actions">
        <button onclick="editRecipe(${r.id})">Edit</button>
        <button class="delete-recipe" onclick="deleteRecipe(${r.id})">
            Delete
        </button>
    </div>

    <button onclick="openPlan(${r.id})">
        Plan meal →
    </button>
</div>
        </article>`).join("");
}

async function toggleFavorite(id) {
    try { await api(`/recipes/${id}/favorite?userId=${userId}`, {method:"PATCH"}); loadRecipes(); }
    catch(e) { toast(e.message); }
}

function openRecipeModal(recipe = null) {
    $("modal").classList.remove("hidden");
    $("modalTitle").textContent = recipe ? "Edit recipe" : "New recipe";
    $("recipeId").value = recipe?.id || "";
    $("title").value = recipe?.title || "";
    $("cuisine").value = recipe?.cuisine || "";
    $("prepMinutes").value = recipe?.prepMinutes ?? 20;
    $("servings").value = recipe?.servings ?? 2;
    $("steps").value = recipe?.steps || "";
    $("ingredientRows").innerHTML = "";
    (recipe?.ingredients?.length ? recipe.ingredients : [{ingredient:{name:""},quantity:"",unit:""}]).forEach(addIngredientRow);
}
function addIngredientRow(item={ingredient:{name:""},quantity:"",unit:""}) {
    const row = document.createElement("div");
    row.className = "ingredient-row";
    row.innerHTML = `<input class="iname" placeholder="Ingredient" value="${escapeHtml(item.ingredient?.name || "")}" required>
                     <input class="iqty" type="number" min="0.01" step="0.01" placeholder="Qty" value="${item.quantity || ""}" required>
                     <input class="iunit" placeholder="Unit" value="${escapeHtml(item.unit || "")}" required>
                     <button type="button" class="remove-ingredient">×</button>`;
    row.querySelector(".remove-ingredient").onclick = () => row.remove();
    $("ingredientRows").appendChild(row);
}
function closeRecipeModal(){ $("modal").classList.add("hidden"); }

$("recipeForm").addEventListener("submit", async e => {
    e.preventDefault();
    const ingredients = [...document.querySelectorAll(".ingredient-row")].map(row => ({
        ingredient:{name:row.querySelector(".iname").value.trim()},
        quantity:Number(row.querySelector(".iqty").value),
        unit:row.querySelector(".iunit").value.trim()
    }));
    const body = {
        title:$("title").value.trim(), cuisine:$("cuisine").value.trim(),
        prepMinutes:Number($("prepMinutes").value), servings:Number($("servings").value),
        steps:$("steps").value.trim(), favorite:false, ingredients
    };
    try {
        const id = $("recipeId").value;
        if (id) {
            const old = recipes.find(r => r.id == id);
            body.favorite = old?.favorite || false;
            await api(`/recipes/${id}?userId=${userId}`, {method:"PUT", body:JSON.stringify(body)});
            toast("Recipe updated");
        } else {
            await api(`/recipes?userId=${userId}`, {method:"POST", body:JSON.stringify(body)});
            toast("Recipe added");
        }
        closeRecipeModal();
        await loadRecipes();
        await loadWeek();
    } catch(e) { toast(e.message); }
});

function editRecipe(id) {
    const recipe = recipes.find(r => r.id === id);
    if (recipe) openRecipeModal(recipe);
}

async function openPlan(recipeId = null, date = today()) {
    $("planModal").classList.remove("hidden");
    $("planDateInput").value = date;
    fillPlanRecipes();
    if (recipeId) $("planRecipe").value = recipeId;
}
function closePlan(){ $("planModal").classList.add("hidden"); }
function fillPlanRecipes() {
    $("planRecipe").innerHTML = recipes.map(r => `<option value="${r.id}">${escapeHtml(r.title)}</option>`).join("");
}

$("planForm").addEventListener("submit", async e => {
    e.preventDefault();
    try {
        await api(`/meal-plans?userId=${userId}`, {
            method:"POST",
            body:JSON.stringify({
                planDate:$("planDateInput").value,
                mealType:$("mealType").value,
                recipe:{id:Number($("planRecipe").value)}
            })
        });
        closePlan(); loadWeek(); toast("Meal added to the week");
    } catch(e) { toast(e.message); }
});

async function loadWeek() {
    try {
        const plans = await api(`/meal-plans?userId=${userId}&date=${$("weekDate").value}`);
        const monday = new Date($("weekDate").value + "T00:00:00");
        const grouped = new Map(plans.map(p => [p.planDate,p]));
        let html = "";
        for(let i=0;i<7;i++){
            const d = new Date(monday); d.setDate(monday.getDate() - ((monday.getDay()+6)%7) + i);
            const key = d.toISOString().slice(0,10), plan = grouped.get(key);
            const label = d.toLocaleDateString("en-IN",{weekday:"short",day:"numeric"});
            html += `<div class="meal-row">
                <div class="meal-day">${label}</div>
                <div>${plan ? `<div class="meal-name">${escapeHtml(plan.recipe.title)}</div><div class="meal-type">${plan.mealType}</div>` : `<span class="muted">No meal</span>`}</div>
                ${plan ? `<button class="delete-plan" onclick="deletePlan(${plan.id})">×</button>` : `<button class="delete-plan" onclick="openPlan(null,'${key}')">+</button>`}
            </div>`;
        }
        $("mealList").innerHTML = html;
    } catch(e) { toast(e.message); }
}
async function deletePlan(id) {
    if (!confirm("Remove this meal from the plan?")) return;
    try { await api(`/meal-plans/${id}?userId=${userId}`, {method:"DELETE"}); loadWeek(); }
    catch(e) { toast(e.message); }
}

async function showShopping() {
    try {
        const list = await api(`/meal-plans/shopping-list?userId=${userId}&date=${$("weekDate").value}`);
        $("shoppingList").innerHTML = list.length ? list.map(x => `<div class="shopping-item"><span>${escapeHtml(x.ingredient)}</span><strong>${x.quantity} ${escapeHtml(x.unit)}</strong></div>`).join("") : `<div class="empty">No planned meals for this week.</div>`;
        $("shoppingModal").classList.remove("hidden");
    } catch(e) { toast(e.message); }
}

$("newRecipeBtn").onclick = () => openRecipeModal();
$("addIngredient").onclick = () => addIngredientRow();
$("closeModal").onclick = closeRecipeModal;
$("cancelBtn").onclick = closeRecipeModal;
$("closePlanModal").onclick = closePlan;
$("cancelPlan").onclick = closePlan;
$("closeShopping").onclick = () => $("shoppingModal").classList.add("hidden");
$("shoppingBtn").onclick = showShopping;
$("weekDate").onchange = loadWeek;
$("refreshBtn").onclick = loadRecipes;
$("allBtn").onclick = () => { currentFavorites=false; $("allBtn").classList.add("active"); $("favBtn").classList.remove("active"); loadRecipes(); };
$("favBtn").onclick = () => { currentFavorites=true; $("favBtn").classList.add("active"); $("allBtn").classList.remove("active"); loadRecipes(); };
let searchTimer;
$("search").oninput = () => { clearTimeout(searchTimer); searchTimer=setTimeout(loadRecipes,300); };
$("userSelect").onchange = e => { userId = Number(e.target.value); loadRecipes(); loadWeek(); };

init();


async function deleteRecipe(id) {
    const recipe = recipes.find(r => r.id == id);
    const name = recipe?.title || "this recipe";

    if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;

    try {
        await api(`/recipes/${id}?userId=${userId}`, { method: "DELETE" });
        toast("Recipe deleted");
        await loadRecipes();
        await loadWeek();
    } catch (err) {
        toast(err.message || "Unable to delete recipe", true);
    }
}
