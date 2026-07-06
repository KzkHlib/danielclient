package com.example.autoclicker.client;

/** Descriptions FR des modules et des réglages (affichées en infobulle). */
public final class Descriptions {

    private Descriptions() {}

    public static String module(String name) {
        if (name.startsWith("AutoClick")) {
            boolean right = name.endsWith("R");
            return "Clique automatiquement (" + (right ? "clic droit" : "clic gauche") + ") à la vitesse réglée.";
        }
        switch (name) {
            case "ClickRecorder": return "Enregistre tes vrais clics gauche dans un fichier .clicks partageable.";
            case "Breadcrumbs":   return "Trace lumineuse derrière toi (chemin parcouru).";
            case "AimAssist":     return "Verrouille l'entité la plus proche et vise pour toi.";
            case "Reach":         return "Étend ta portée d'attaque au-delà des 3 blocs vanilla.";
            case "NoSlowdown":    return "Plus de ralentissement en mangeant / bloquant / bandant l'arc.";
            case "Xray":          return "Rend le monde translucide, seuls les minerais whitelistés restent pleins.";
            case "ChestESP":      return "Affiche les coffres normaux et pieges avec box et tracer.";
            case "NoFall":        return "Annule les dégâts de chute.";
            case "Parkour":       return "Saute automatiquement au bord des blocs.";
            case "SafeWalk":      return "T'empêche de tomber des bordures (Normal/Eagle/Sneak).";
            case "ESP":           return "Surligne les entités (boîte 2D ou 3D).";
            case "Tracers":       return "Trace une ligne du viseur vers chaque entité.";
            case "Nametags":      return "Tag complet : pseudo, vie, stuff, enchants et effets.";
            case "ViewModel":     return "Décale et oriente l'item tenu (main custom).";
            case "Animations":    return "Style d'animation fluide de l'item tenu (12 styles).";
            case "Friends":       return "Vise un joueur + touche (B) pour l'ajouter en ami.";
            case "Flight":        return "Vol libre, vitesses horizontale/verticale réglables.";
            case "Timer":         return "Accélère ou ralentit l'horloge du jeu.";
            case "FastUse":       return "Mange/boit/bande l'arc plus vite.";
            case "AntiDebuff":    return "Retire les effets de potion négatifs (client).";
            case "Velocity":      return "Réduit/modifie le knockback (Cancel/Push/Humanize/Legit/Décalé) avec jitter et randomisation.";
            case "KillAura":      return "Frappe automatiquement la meilleure cible à portée.";
            case "RodMacro":      return "Lance une rod apres chaque hit puis revient a l'epee.";
            case "AutoBlock":     return "Bloque automatiquement à l'épée (1.8).";
            case "BowAimbot":     return "Vise auto à l'arc (gravité + prédiction) pendant la charge.";
            case "W-Tap":         return "Coupe le sprint au hit = knockback maximal.";
            case "AntiAFK":       return "Bouge/tourne/saute automatiquement pour éviter le kick AFK.";
            case "AntiBot":       return "Ignore les faux joueurs (bots) pour Aim / ESP / Nametags.";
            case "AntiVoid":      return "Te sauve si tu tombes dans le vide (Fallback/Motion/Packet).";
            case "AutoPot":       return "Lance une potion soin quand tes PV sont bas (silencieux).";
            case "AutoArmor":     return "Équipe automatiquement la meilleure armure de l'inventaire.";
            case "AutoHeal":      return "Utilise auto potion/soupe/gapple quand les PV sont sous le seuil.";
            case "AutoRespawn":   return "Respawn automatique après la mort avec délai réglable.";
            case "AutoRod":       return "Lance la canne à pêche automatiquement pour reset le combo.";
            case "AutoSoup":      return "Mange soupe/nourriture quand PV bas + drop du bol auto.";
            case "ChestStealer":  return "Recupere automatiquement le contenu des coffres ouverts.";
            case "Step":          return "Monte les blocs automatiquement (hauteur réglable).";
            case "Sprint":        return "Sprint automatique (option omni-directionnel).";
            case "Blink":         return "Retient tes paquets de mouvement, puis téléporte à la désactivation.";
            case "LavaMacro":     return "Pose un bucket de lave sous la cible et reprend la source (touche G).";
            case "AutoWater":     return "Pose de l'eau sous tes pieds quand tu brûles, puis reprend.";

            case "Criticals":     return "Force des coups critiques (packets de saut).";
            case "KeepSprint":    return "Garde le sprint après un coup (KB constant).";
            case "TargetStrafe":  return "Tourne automatiquement autour de la cible.";
            case "Speed":         return "Déplacement rapide / Bhop (plusieurs configs).";
            case "CustomCrosshair": return "Viseur personnalisé au centre de l'écran.";
            case "Interface":     return "Change la couleur d'accent du client (HSB / rainbow).";
            case "ComboCounter":  return "Compte tes coups d'affilée (reset si tu te fais toucher).";
            case "ReachDisplay":  return "Affiche la distance de ton dernier coup (info).";
            case "HitParticles":  return "Particules cosmétiques sur la cible quand tu frappes.";
            case "FastPlace":     return "Pose les blocs / clics droits plus vite.";
            case "InvCleaner":    return "Jette automatiquement les items poubelle de l'inventaire.";
            case "ItemESP":       return "Affiche les items au sol avec leur nom et rareté.";
            case "FastBreak":     return "Enchaîne le minage des blocs plus vite.";
            case "AutoTool":      return "Passe au meilleur outil pour casser le bloc visé.";
            case "BlockOverlay":  return "Contour coloré stylé sur le bloc visé.";
            case "FreeLook":      return "Regarde autour sans tourner ton perso (maintien).";
            case "Fullbright":    return "Luminosité au maximum, même dans le noir.";
            case "NoHurtCam":     return "Enlève la secousse de caméra quand tu prends des dégâts.";
            case "Zoom":          return "Zoome tant que tu maintiens la touche.";
            case "ToggleSprint":  return "Sprinte tout seul sans tenir la touche.";
            case "ToggleSneak":   return "Reste accroupi sans tenir la touche.";
            case "CPS":           return "Affiche ton nombre de clics par seconde.";
            case "ArrayList":     return "Liste les modules activés à l'écran.";
            case "TargetHUD":     return "Vie et distance de la cible que tu vises.";
            case "PotionHUD":     return "Tes effets de potion actifs avec leur durée.";
            case "ArmorHUD":      return "Ton armure et sa durabilité.";
            case "HitBox":        return "Agrandit la hitbox des entités pour toucher plus facilement.";
            case "FastLadder":    return "Monte les échelles plus vite (plusieurs modes).";
            case "LongJump":      return "Saut longue distance (NCP/AAC/Verus).";
            case "Jesus":         return "Marche sur l'eau / la lave.";
            case "Spider":        return "Grimpe aux murs comme une araignée.";
            case "Trajectories":  return "Affiche la trajectoire de l'arc / projectiles.";
            case "Chams":         return "Entités colorées à travers les murs.";
            case "Scaffold":      return "Place des blocs sous tes pieds en marchant.";
            case "SuperKnockback":return "Multiplie/inverse/annule le knockback infligé.";
            case "TriggerBot":    return "Frappe automatiquement l'entité dans le viseur.";
            case "AntiHunger":    return "Réduit la perte de faim / spoof food.";
            case "Clip":          return "Téléporte verticalement/horizontalement (VClip/HClip).";
            case "Phase":         return "Traverse les murs (NCP/AAC/Verus).";
            case "WebKick":       return "Sort rapidement des toiles d'araignée.";
            case "IceSpeed":      return "Glisse plus vite sur la glace.";
            case "Strafe":        return "Contrôle amélioré du strafe aérien.";
            case "WTap":          return "Reset de combo (W-Tap / S-Tap / D-Tap / Auto).";
            case "Notifications": return "Petite notif animée à chaque (dés)activation.";
            default:              return "";
        }
    }

    /** Description d'un réglage ; le nom du module désambiguïse les homonymes (ex. FOV). */
    public static String setting(String moduleName, String name) {
        if (name.equals("FOV") && moduleName.equals("AimAssist"))
            return "Cône d'acquisition autour du viseur (360 = tout autour).";
        if (name.equals("Portee") && moduleName.equals("Reach"))
            return "Portée d'attaque sur les entités (blocs).";
        if (name.equals("Delai") && moduleName.equals("Velocity"))
            return "Ticks avant d'appliquer le knockback (mode Décalé).";
        if (name.equals("Vitesse") && moduleName.equals("Timer"))
            return "Multiplicateur d'horloge (>1 plus rapide).";
        if (name.equals("Vitesse") && moduleName.equals("FastUse"))
            return "Vitesse d'utilisation de l'objet.";
        if (name.equals("Profil") && moduleName.startsWith("AutoClick"))
            return "Preset de timing : Premium, Stable, Jitter, Butterfly ou Drag.";
        if (moduleName.equals("Xray")) {
            if (name.equals("Opacite")) return "Opacité des blocs hors whitelist (0 = invisibles).";
            if (name.endsWith("Ore"))   return "Garde ce minerai visible et plein.";
        }
        if (name.equals("Rotation silencieuse") && moduleName.equals("Scaffold"))
            return "Garder la cam\u00e9ra libre pendant le scaffold.";
        switch (name) {
            case "Forme":             return "Boîte 2D (face caméra) ou 3D (filaire).";
            case "Couleur":           return "Par type (rouge/vert/blanc) ou couleur accent.";
            case "Coffres normaux":   return "Affiche les coffres classiques.";
            case "Coffres pieges":    return "Affiche les coffres pieges.";
            case "Tracer":            return "Trace une ligne du viseur vers le coffre.";
            case "Stuff + main":      return "Affiche l'armure et l'item tenu.";
            case "Enchants détaillés": return "Liste les enchants exacts (Sharpness V…).";
            case "Effets":            return "Affiche les effets de potion actifs.";
            case "Taille":            return "Échelle du tag à l'écran.";
            case "Horizontale":       return "Vitesse de vol horizontale.";
            case "Verticale":         return "Vitesse de vol verticale.";
            case "Hover":             return "Reste immobile en l'air sans input.";
            case "Rotation silencieuse": return "Restaure ta vue après le cycle (snap bref).";
            case "Reprendre eau":     return "Reprend la source d'eau après extinction.";
            case "Delai reprise":     return "Ticks d'attente avant de reprendre l'eau.";
            case "Smoothness":        return "Douceur de la visée (haut = plus lent/fluide).";
            case "Vitesse H":         return "Vitesse max horizontale de la visée (deg/tick).";
            case "Vitesse V":         return "Vitesse max verticale de la visée (deg/tick).";
            case "Stick":             return "Aimante la visée quand la cible est proche.";
            case "Prediction":        return "Anticipe le déplacement de la cible.";
            case "Pitch doux":        return "Adoucit davantage l'axe vertical.";
            case "Seulement en cliquant": return "N'aide à viser que clic gauche maintenu.";
            case "Miss %":            return "% de clics volontairement sautés (humain).";
            case "Micro-pauses":      return "Petites pauses périodiques (humain).";
            case "Double clic %":     return "% de chance d'un second clic immédiat (rafale).";
            case "Drift CPS":         return "Variation lente du CPS cible pendant le maintien.";
            case "Fatigue":           return "Ralentit tres legerement les longs maintiens.";
            case "Pause chance":      return "Chance de petite pause apres un clic.";
            case "Pause min ms":      return "Duree minimale d'une micro-pause.";
            case "Pause max ms":      return "Duree maximale d'une micro-pause.";
            case "Burst %":           return "Chance d'une courte rafale de clics.";
            case "Burst clics":       return "Nombre de clics dans une rafale.";
            case "Arme seulement":    return "Clic gauche uniquement avec epee ou hache en main.";
            case "Source clics":      return "Premium genere le timing, Recording utilise ton fichier de clics.";
            case "Recorder":          return "Record capture tes vrais clics puis sauvegarde un fichier local.";
            case "Replay jitter":     return "Variation appliquee au pattern en mode Recording.";
            case "Break block":       return "Autorise l'AutoClickL a cliquer aussi sur les blocs.";
            case "Delai rod ticks":   return "Ticks apres le hit avant d'envoyer la rod.";
            case "Retour ticks":      return "Ticks avant de revenir a l'epee.";
            case "Cooldown ms":       return "Temps minimum entre deux rods.";
            case "Slot rod":          return "0 = auto, 1-9 = slot hotbar force.";
            case "Slot sword":        return "0 = auto, 1-9 = slot hotbar force.";
            case "Epee requise":      return "Ne lance la rod que si tu hit avec une epee.";
            case "Retour sword":      return "Revient automatiquement sur l'epee apres la rod.";
            case "Swing rod":         return "Joue l'animation de swing quand la rod part.";
            case "Annuler si occupe": return "Ignore un nouveau hit si une sequence rod est deja en cours.";
            case "Notif manque rod":  return "Affiche une notif si aucune rod n'est trouvee.";
            case "Min interval ms":   return "Ignore les intervalles trop courts pour eviter les 0ms/double edges.";
            case "Max interval ms":   return "Ignore les longues pauses hors pattern.";
            case "Priorité":          return "Choix de cible : distance / vie la plus basse / angle.";
            case "Délai switch":      return "Délai avant de viser une nouvelle cible (anti-snap).";
            case "Rotation":          return "Caméra (lissée) / Silencieuse (packet) / Aucune.";
            case "Smooth":            return "Vitesse de rotation (plus haut = plus rapide).";
            case "Auto-block":        return "Bloque à l'épée entre les coups.";
            case "Activation":        return "Toujours, ou seulement en cliquant.";
            case "Absent du tab":     return "Marque bot si absent de la liste des joueurs.";
            case "Nom invalide":      return "Marque bot si le pseudo est invalide.";
            case "Prédiction":        return "Anticipe le déplacement de la cible.";
            case "Mode cible":        return "Single : garde la cible. Switch : toujours la meilleure.";
            case "Seuil PV":          return "PV en dessous desquels lancer la potion.";
            case "Soin uniquement":   return "Ne lance que des potions de soin/régen.";
            case "Cooldown":          return "Délai (ticks) entre deux potions.";
            case "Hauteur":           return "Hauteur de marche automatique (blocs).";
            case "Omni-directionnel": return "Sprint aussi en arrière / sur les côtés.";
            case "Drop bol auto":     return "Jette automatiquement les bols vides.";
            case "Garde 1 slot":      return "Ne consomme pas la dernière soupe/nourriture.";
            case "Delai ouverture":   return "Temps d'attente apres l'ouverture du coffre.";
            case "Delai clic":        return "Temps entre deux vagues de recuperation.";
            case "Recuperation ms":   return "Delai en millisecondes entre deux vagues de loot.";
            case "Items/tick":        return "Nombre d'items recuperes a chaque vague.";
            case "Profil":            return "Instant vide le coffre d'un coup, Rapide/Legit appliquent des delais.";
            case "Slots aleatoires":  return "Recupere les slots dans un ordre aleatoire.";
            case "Fermer vide":       return "Ferme le coffre automatiquement quand il est vide.";
            case "Plastron dia Thorns1": return "Prend les plastrons diamant avec Thorns I.";
            case "Pantalon dia Thorns1": return "Prend les pantalons diamant avec Thorns I.";
            case "Casque dia Thorns1": return "Prend les casques diamant avec Thorns I.";
            case "Sword dia Sharp1":   return "Prend les epees diamant avec Sharpness I.";
            case "Lingots or":         return "Prend les lingots d'or.";
            case "Bouteilles XP":      return "Prend les bouteilles d'XP.";
            case "Saut auto":         return "Saute automatiquement (Bhop).";
            case "Rayon":             return "Distance maintenue autour de la cible.";
            case "Sol uniquement":    return "Ne strafe que quand tu es au sol.";
            case "Teinte":            return "Teinte de la couleur d'accent.";
            case "Saturation":        return "Saturation de la couleur d'accent.";
            case "Luminosite":        return "Luminosité de la couleur d'accent.";
            case "Rainbow":           return "Cycle arc-en-ciel automatique.";
            case "Ecart":             return "Espace central du viseur.";
            case "Couleur accent":    return "Utilise la couleur d'accent du client.";
            case "Speed":             return "Vitesse des animations qui tournent (Spin, Helico, Screw…).";
            case "Vitesse spin":      return "Vitesse des animations qui tournent (Spin, Helico, Screw…).";
            case "Modele 3D":         return "Affiche le modèle (skin + armure) de la cible.";
            case "Stuff + dura":      return "Affiche l'équipement et sa durabilité.";
            case "Opacite fond":      return "Transparence du fond du TargetHUD.";
            case "Sword":             return "Pas de ralentissement en bloquant à l'épée.";
            case "Bow":               return "Pas de ralentissement en bandant l'arc.";
            case "Eat":               return "Pas de ralentissement en mangeant/buvant.";
            case "Randomisation":     return "Variation aléatoire du timing de clic (%).";
            case "Horizontal":        return "% de knockback horizontal conservé (0 = aucun).";
            case "Vertical":          return "% de knockback vertical conservé (0 = aucun).";
            case "CPS min":         return "Vitesse de clic minimale.";
            case "CPS max":         return "Vitesse de clic maximale.";
            case "Mode":            return "Maintien : clic tant que le bouton est tenu. Toggle : clic en continu.";
            case "Clic inventaire": return "Autorise aussi les clics dans les menus.";
            case "Methode":         return "Normal, Jitter (saccadé) ou Butterfly (par paires).";
            case "Historique":      return "Nombre de positions enregistrées dans le buffer.";
            case "Fantomes":        return "Affiche les positions fantômes dans le monde.";
            case "Nb fantomes":     return "Nombre de fantômes affichés simultanément.";
            case "Envoi %":         return "Probabilité d'envoyer un C03 décalé à chaque tick.";
            case "GCD correction":   return "Snap les rotations au pas natif de la souris (GCD) pour éviter les checks anti-aim.";
            case "Micro-ajust":      return "Simule de micro-corrections humaines de la visée.";
            case "Reach min":        return "Portée minimale (reach dynamique).";
            case "Reach max":        return "Portée maximale (reach dynamique).";
            case "Prediction hitbox": return "Anticipe le déplacement de la cible pour étendre la portée.";
            case "Bonus prediction": return "Bonus de portée max ajouté quand la cible s'approche.";
            case "Reach dynamique":  return "Fait varier la portée entre min et max à chaque hit.";
            case "Miss streak":      return "Saute un clic tous les N clics (simule l'imprécision).";
            case "Courbe CPS":      return "Progression progressive du CPS au début du clic (courbe d'accélération).";
            case "CPS Polar":       return "CPS cible pour le profil Polar (humanisé).";
            case "Humaniser timing": return "Distribution de timing plus réaliste avec fatigue.";
            case "Portee":          return "Distance max de verrouillage (blocs).";
            case "Smooth horizontal": return "Vitesse max de rotation horizontale (degrés/tick).";
            case "Smooth vertical": return "Vitesse max de rotation verticale (degrés/tick).";
            case "Cibles":          return "Type d'entités visées : mobs, joueurs ou tout.";
            case "Garder cible":    return "Garde la même cible tant qu'elle est valable.";
            case "A travers murs":  return "Autorise le verrouillage sans ligne de vue.";
            case "FOV":             return "Champ de vision en zoom (plus petit = plus zoomé).";
            case "Delai":           return "Ticks entre deux poses (0 = le plus rapide).";
            case "Epaisseur":       return "Épaisseur du contour du bloc.";
            case "Remplir":         return "Remplit le bloc visé en plus du contour.";
            case "PV min":          return "Seuil de santé pour déclencher le soin automatique.";
            case "Pause bow":       return "Ne pas se soigner quand l'arc est bandé.";
            case "Seuil Y":         return "Hauteur Y maximale pour activer l'AntiVoid.";
            case "Invulnérable":    return "Temps avant de commencer à nettoyer (ticks).";
            case "Labels":          return "Affiche le nom des items en label 3D.";
            case "Boîtes":          return "Affiche une boîte de sélection autour des items.";
            case "Durée ticks":     return "Durée de vie du trail en ticks.";
            case "Arc-en-ciel":     return "Dégradé arc-en-ciel sur le trail.";
            case "Taille X":        return "Expansion horizontale de la hitbox.";
            case "Taille Y":        return "Expansion verticale de la hitbox.";
            case "Boost":           return "Multiplicateur de distance du saut.";
            case "Hauteur saut":    return "Hauteur initiale du saut.";
            case "Visible":         return "Affiche l'entité normalement (texture).";
            case "À travers les murs": return "Affiche l'entité à travers les murs.";
            case "Rouge":           return "Teinte rouge de la couleur Chams.";
            case "Vert":            return "Teinte verte de la couleur Chams.";
            case "Bleu":            return "Teinte bleue de la couleur Chams.";
            case "Alpha":           return "Transparence du Chams (0 = invisible, 1 = opaque).";
            case "Tower":           return "Saute et place un bloc sous toi (tourelle).";
            case "Sous-mode":       return "Sous-mode (Legit, Instant, AAC, etc.).";
            case "Joueurs":         return "Cible les joueurs.";
            case "Mobs":            return "Cible les mobs.";
            case "Moins de faim":   return "Réduit activement la perte de faim.";
            case "Direction":       return "Up/Down/Forward/Back/Left/Right.";
            case "Air seulement":   return "N'active le strafe qu'en l'air.";
            case "Force":           return "Puissance du strafe.";
            case "Bypass":          return "Mode bypass anti-cheat.";
            case "Eagle":           return "0=désactivé, 1=bordure, 2=toujours.";
            case "Expand":          return "Place aussi en diagonale pour allonger.";
            case "Smart":           return "N'étend la hitbox que quand tu vises une entité.";
            case "Always":          return "Étend la hitbox même hors visée.";
            case "Pot mode":        return "Sous-mode potion (Normal/Jump/NCP/AAC).";
            case "Soup mode":       return "Sous-mode soupe (Normal/Instant/AAC).";
            case "Gapp mode":       return "Sous-mode golden apple (Normal/Packet).";
            case "Distance max":    return "Distance de chute maximale avant activation.";
            case "Delai ticks":     return "Ticks d'attente avant exécution.";
            default:                return "";
        }
    }
}
