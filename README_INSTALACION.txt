D0cCtor's Lives 1.0.0-fix13

Compilar:
  gradle clean build

Cambio:
- Sacado completamente el render de vidas arriba del jugador/nameplate.
- Ya no se dibujan corazones sobre el nick del jugador.
- El TAB tampoco muestra vidas.
- El HUD arriba a la izquierda sigue funcionando.
- El sistema de vidas, limbo, fragmento, efectos y mensajes globales se mantiene igual.

Estado visual:
- HUD personal: activo.
- TAB vidas: desactivado.
- Nameplate vidas: desactivado.


--- README anterior ---
D0cCtor's Lives 1.0.0-fix12

Compilar:
  gradle clean build

Cambios:
- Saqué completamente las vidas del TAB:
  - no número amarillo
  - no barra de corazones
  - no overlay raro
- El scoreboard d0_lives queda solo interno/network.
- Ahora los corazones se dibujan arriba del nick/nameplate de cada jugador en el mundo.
  - No usa teams, así que no debería aparecer en el chat.
  - Usa la info sincronizada por network.
- Al morir:
  - ya no le manda mensaje privado al jugador con las vidas restantes.
  - solo manda mensaje global al chat:
    "Jugador murió y perdió 1 vida. Ahora tiene X/Y vidas."
  - si llega a 0:
    "Jugador murió y agotó el ciclo. Ahora tiene 0/Y vidas."
- Mantiene:
  - HUD arriba a la izquierda
  - efecto al respawnear
  - fragmento con solo mensaje global
  - limbo sin tocar spawnpoint


--- README anterior ---
D0cCtor's Lives 1.0.0-fix11

Compilar:
  gradle clean build

Cambio del TAB:
- Confirmado: rendertype hearts vanilla siempre dibuja una barra completa de 10 corazones.
- Por eso lo saqué.
- El server vuelve a mostrar d0_lives como scoreboard normal en TAB.
- El cliente tapa el número amarillo y dibuja encima:
  ❤❤❤❤
  ❤❤❤♡
  ❤❤♡♡
  ❤♡♡♡
- No usa teams, así que no aparece en el chat.
- Mantiene el TAB normal de Minecraft, solo reemplaza visualmente el número por corazones.

Si queda corrido visualmente, solo hay que ajustar la coordenada X/Y del overlay.


--- README anterior ---
D0cCtor's Lives 1.0.0-fix10

Compilar:
  gradle clean build

Cambio único:
- El TAB ya no muestra el número amarillo 4.
- Ahora d0_lives usa:
  scoreboard objectives modify d0_lives rendertype hearts
- El score del TAB se manda como medios corazones:
  4 vidas = score 8 = 4 corazones llenos
  3 vidas = score 6 = 3 corazones llenos
  2 vidas = score 4 = 2 corazones llenos
  1 vida = score 2 = 1 corazón lleno

Mantiene todo lo de fix9:
- Fragmento solo con mensaje global.
- Efecto al respawnear con darkness/slowness/sonidos/partículas.
- Sin teams, para que los corazones no aparezcan en el chat.


--- README anterior ---
D0cCtor's Lives 1.0.0-fix9

Compilar:
  gradle clean build

Cambios:
- Fragmento del Ciclo:
  ahora solo manda mensaje global al chat.
  Ya no manda mensaje privado adicional al jugador.
- Muerte / pérdida de vida:
  el efecto sigue ejecutándose al respawnear.
  Agregué darkness 3 segundos.
  Agregué slowness 3 segundos.
  Cambié sonidos a una mezcla más oscura:
    entity.warden.heartbeat
    block.sculk_shrieker.shriek
    block.respawn_anchor.deplete
  Agregué partículas:
    sculk_soul
    damage_indicator
- TAB:
  saqué teams/suffix porque por Minecraft también salen en el chat.
  ahora usa scoreboard vanilla en TAB:
    /scoreboard objectives setdisplay list d0_lives
  y el objetivo tiene displayname de corazón rojo.
  Nota: vanilla scoreboard muestra el número de vidas. No permite reemplazar ese número por varios iconos sin usar teams o render custom.
- Limbo:
  mantiene lo anterior: no cambia spawnpoint al entrar al limbo.
  al salir intenta volver al spawn normal/cama del jugador.


--- README anterior ---
D0cCtor's Lives 1.0.0-fix8.1

Compilar:
  gradle clean build

Fix:
- Arreglado error de compilación:
  Player.findRespawnPositionAndUseSpawnBlock no existe con ese nombre/firma en NeoForge 1.21.1.
- La salida del limbo ahora usa:
  - getRespawnDimension()
  - getRespawnPosition()
  - getRespawnAngle()
- Si el jugador tiene cama/spawn personal, lo manda ahí.
- Si no tiene spawn personal, lo manda al spawn vanilla del mundo.
- Si algo falla, usa /vidas settown como backup.

Mantiene cambios de fix8:
- Totem real con icono de corazón roto.
- TAB normal estilo script viejo con teams/suffix.
- Limbo sin cambiar spawnpoint del jugador.


--- README anterior ---
D0cCtor's Lives 1.0.0-fix8 - TAB viejo + Totem real + spawn normal

Compilar:
  gradle clean build

Cambios:
- Efecto de perder vida:
  ahora reproduce literalmente la animación vanilla tipo Totem,
  pero usando un item técnico con textura de corazón roto.
- El efecto se dispara al respawnear, no al morir.
- TAB:
  volví al sistema del script viejo de KubeJS:
  teams con suffix según vidas.
  Ejemplo: jugador ❤❤♡♡
- Se eliminó el overlay raro del TAB.
- El TAB vuelve a ser el TAB normal de Minecraft.
- Limbo:
  ya no cambiamos el spawnpoint del jugador para mandarlo al limbo.
  Solo lo teletransportamos al limbo.
- Salida del limbo:
  intenta mandarlo a su spawn normal real:
  cama si tiene cama, si no spawn vanilla del mundo.
  Si algo falla, cae como backup en /vidas settown.
- Fragmento del Ciclo:
  si lo usa estando en limbo, recupera vida y vuelve a su spawn normal.
