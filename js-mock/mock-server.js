const express = require('express');
const app = express();
const port = 9090;

app.use(express.json());

// Внутреннее состояние инстанса для дебага
let instanceState = {
    isAuthorized: false,
    phoneAttached: "994509998877@c.us",
    qrRequestCount: 0,
    simulateAlreadyLogged: false
};

function resetState() {
    instanceState.isAuthorized = false;
    instanceState.phoneAttached = "994509998877@c.us";
    instanceState.qrRequestCount = 0;
    instanceState.simulateAlreadyLogged = false;
    console.log("\n[Mock System] State has been RESET to default.");
}

// ==========================================
// 🛠 ЭНДПОИНТЫ ДЛЯ УПРАВЛЕНИЯ МОКОМ (ДЛЯ ВАС)
// ==========================================

app.get('/mock/trigger-scan', (req, res) => {
    instanceState.isAuthorized = true;
    instanceState.phoneAttached = "994501234567@c.us";
    console.log("\n🔥 [Mock Trigger] User scanned QR! Instance is now AUTHORIZED.");
    res.send("Status changed to AUTHORIZED. Check your Java logs for long-polling updates!");
});

app.get('/mock/trigger-collision', (req, res) => {
    instanceState.simulateAlreadyLogged = true;
    instanceState.isAuthorized = true;
    instanceState.phoneAttached = "3333333333@c.us";
    console.log("\n🔥 [Mock Trigger] Collision scenario enabled. Next QR request will return alreadyLogged.");
    res.send("Collision scenario enabled.");
});

app.get('/mock/reset', (req, res) => {
    resetState();
    res.send("Reset successful.");
});

// ==========================================
// 🍏 ОСНОВНЫЕ ЭНДПОИНТЫ GREEN-API (ЧЕРЕЗ MIDDLEWARE, ЧТОБЫ КАТЕГОРИЧЕСКИ ИЗБЕЖАТЬ ИШЬЮ С РУТИНГОМ)
// ==========================================

app.use((req, res, next) => {
    const url = req.url;

    // Перехватываем метод qrCode
    if (url.includes('/qrCode/') && req.method === 'GET') {
        instanceState.qrRequestCount++;
        console.log(`[GreenAPI - QR] Request #${instanceState.qrRequestCount} for URL: ${url}`);

        if (instanceState.simulateAlreadyLogged || instanceState.qrRequestCount % 3 === 0) {
            console.log(`[GreenAPI - QR] Returning 'alreadyLogged' error`);
            return res.json({ "type": "alreadyLogged", "message": "Instance already logged" });
        }

        if (instanceState.isAuthorized) {
            return res.json({ "type": "alreadyLogged", "message": "Instance already logged" });
        }

        return res.json({
            "type": "qrCode",
            "message": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        });
    }

    // Перехватываем метод getStateInstance
    if (url.includes('/getStateInstance/') && req.method === 'GET') {
        const status = instanceState.isAuthorized ? "authorized" : "notAuthorized";
        console.log(`[GreenAPI - State] Current status: ${status}`);
        return res.json({ "statusInstance": status });
    }

    // Перехватываем метод getSettings
    if (url.includes('/getSettings/') && req.method === 'GET') {
        console.log(`[GreenAPI - Settings] Returning phone: ${instanceState.phoneAttached}`);
        return res.json({ "wid": instanceState.phoneAttached, "countryInstance": "AZ" });
    }

    // Перехватываем метод logout
    if (url.includes('/logout/') && req.method === 'POST') {
        console.log(`[GreenAPI - Logout] Pruning old session... CLEARING STATE`);
        instanceState.isAuthorized = false;
        instanceState.simulateAlreadyLogged = false;
        instanceState.phoneAttached = "";
        return res.json({ "isLogout": true });
    }

    next();
});

// Запуск сервера
const server = app.listen(port, () => {
    console.log(`\n🚀 Advanced Mock Green-API active on http://localhost:${port}`);
    console.log(`Use browser or Postman to change scenarios on the fly:`);
    console.log(`  🔗 http://localhost:9090/mock/trigger-scan      -> Simulate phone scanned QR`);
    console.log(`  🔗 http://localhost:9090/mock/trigger-collision -> Simulate 'alreadyLogged' with stranger phone`);
    console.log(`  🔗 http://localhost:9090/mock/reset             -> Reset mock state\n`);
});

// КРИТИЧЕСКИЙ ХАК ДЛЯ ТВОЕГО ОКРУЖЕНИЯ: Принудительно держим Node.js процесс живым, если Express пытается закрыться
setInterval(() => {}, 1000 << 30);
