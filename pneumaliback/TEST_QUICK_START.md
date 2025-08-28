# 🚀 Guide de Démarrage Rapide - Test de la Sécurité

## Prérequis
- Java 21 installé
- Maven installé
- PostgreSQL en cours d'exécution
- Base de données `pneumaliback` créée

## 1. Démarrage de l'Application

```bash
cd pneumaliback
mvn spring-boot:run
```

L'application démarre sur `http://localhost:9999`

## 2. Vérification des Utilisateurs Créés

Au démarrage, vous devriez voir dans les logs :
```
=== UTILISATEURS PAR DÉFAUT CRÉÉS ===
ADMIN: admin@pneumali.ml / admin123
CLIENT: client@pneumali.ml / client123
INFLUENCEUR: influenceur@pneumali.ml / influenceur123
=====================================
```

## 3. Test de l'API Swagger

Ouvrez votre navigateur et allez sur :
```
http://localhost:9999/swagger-ui/index.html
```

## 4. Tests Rapides avec cURL

### Test 1: Endpoint Public (sans authentification)
```bash
curl -X GET http://localhost:9999/api/test/public
```
**Résultat attendu**: `"Cet endpoint est public - accessible à tous"`

### Test 2: Endpoint Protégé (sans authentification)
```bash
curl -X GET http://localhost:9999/api/test/authenticated
```
**Résultat attendu**: `401 Unauthorized`

### Test 3: Connexion Admin
```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pneumali.ml","password":"admin123"}'
```
**Résultat attendu**: Token JWT dans la réponse

### Test 4: Accès Admin avec Token
```bash
# Remplacez <TOKEN> par le token reçu précédemment
curl -X GET http://localhost:9999/api/test/admin \
  -H "Authorization: Bearer <TOKEN>"
```
**Résultat attendu**: `"Bonjour admin@pneumali.ml ! Vous avez accès aux fonctionnalités admin."`

### Test 5: Accès Client avec Token Admin
```bash
curl -X GET http://localhost:9999/api/test/client \
  -H "Authorization: Bearer <TOKEN>"
```
**Résultat attendu**: `"Bonjour admin@pneumali.ml ! Vous avez accès aux fonctionnalités client."`

### Test 6: Connexion Client
```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"client@pneumali.ml","password":"client123"}'
```

### Test 7: Accès Admin avec Token Client
```bash
# Remplacez <TOKEN_CLIENT> par le token client
curl -X GET http://localhost:9999/api/test/admin \
  -H "Authorization: Bearer <TOKEN_CLIENT>"
```
**Résultat attendu**: `403 Forbidden`

## 5. Test des Endpoints d'Administration

### Récupération de la Liste des Utilisateurs
```bash
curl -X GET http://localhost:9999/api/admin/users \
  -H "Authorization: Bearer <TOKEN_ADMIN>"
```

### Statistiques des Utilisateurs
```bash
curl -X GET http://localhost:9999/api/admin/stats \
  -H "Authorization: Bearer <TOKEN_ADMIN>"
```

## 6. Test des Endpoints Influenceur

### Connexion Influenceur
```bash
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"influenceur@pneumali.ml","password":"influenceur123"}'
```

### Accès au Profil Influenceur
```bash
curl -X GET http://localhost:9999/api/influenceur/profile \
  -H "Authorization: Bearer <TOKEN_INFLUENCEUR>"
```

## 7. Test de Sécurité

### Test de Verrouillage de Compte
```bash
# Tentatives de connexion échouées
for i in {1..6}; do
  curl -X POST http://localhost:9999/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"client@pneumali.ml","password":"mauvais_mot_de_passe"}'
done
```

Après 5 tentatives échouées, le compte sera verrouillé.

## 8. Vérification des Logs

Regardez les logs de l'application pour voir :
- Les tentatives de connexion
- Les accès aux endpoints
- Les erreurs de sécurité
- Les actions d'administration

## 9. Test de Validation

### Test avec Données Invalides
```bash
curl -X POST http://localhost:9999/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"email_invalide","password":"123","confirmPassword":"456"}'
```

**Résultat attendu**: `400 Bad Request` avec messages de validation

## 10. Test de Déconnexion

```bash
curl -X POST http://localhost:9999/api/auth/logout \
  -H "Authorization: Bearer <TOKEN>"
```

## 🎯 Points de Vérification

✅ **Sécurité de base** : Endpoints protégés retournent 401/403  
✅ **Authentification JWT** : Tokens valides permettent l'accès  
✅ **Autorisation par rôle** : Chaque rôle accède à ses ressources  
✅ **Validation des données** : Erreurs 400 pour données invalides  
✅ **Verrouillage de compte** : Compte verrouillé après 5 échecs  
✅ **Logs de sécurité** : Toutes les actions sont tracées  
✅ **Gestion des erreurs** : Messages d'erreur appropriés  

## 🚨 En Cas de Problème

1. **Vérifiez les logs** de l'application
2. **Vérifiez la base de données** PostgreSQL
3. **Vérifiez la configuration** dans `application.properties`
4. **Redémarrez l'application** si nécessaire

## 📚 Documentation Complète

Pour plus de détails, consultez le fichier `SECURITY_README.md`
