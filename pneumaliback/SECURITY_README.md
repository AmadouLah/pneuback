# Guide de Sécurité - PneuMali Backend

## Vue d'ensemble

Cette application utilise Spring Security avec JWT pour une authentification et autorisation robustes. La sécurité est implémentée de bout en bout avec les meilleures pratiques.

## Architecture de Sécurité

### 1. Authentification JWT
- **Access Token** : Valide 24 heures
- **Refresh Token** : Valide 7 jours
- **Signature** : HMAC-SHA256 avec clé secrète configurable

### 2. Gestion des Rôles
- **ADMIN** : Accès complet à toutes les fonctionnalités
- **CLIENT** : Accès aux fonctionnalités client
- **INFLUENCEUR** : Accès aux fonctionnalités influenceur

### 3. Protection contre les Attaques
- **Verrouillage de compte** : Après 5 tentatives de connexion échouées
- **Durée de verrouillage** : 30 minutes
- **Validation des mots de passe** : Minimum 8 caractères
- **Encodage des mots de passe** : BCrypt

## Endpoints de Sécurité

### Authentification Publique
```
POST /api/auth/register - Inscription
POST /api/auth/login - Connexion
POST /api/auth/refresh - Renouvellement de token
POST /api/auth/logout - Déconnexion
```

### Endpoints de Test
```
GET /api/test/public - Accessible à tous
GET /api/test/authenticated - Utilisateurs connectés
GET /api/test/admin - Administrateurs uniquement
GET /api/test/client - Clients uniquement
GET /api/test/influenceur - Influenceurs uniquement
```

### Endpoints d'Administration (Admin uniquement)
```
GET /api/admin/users - Liste de tous les utilisateurs
GET /api/admin/users/{id} - Détails d'un utilisateur
GET /api/admin/users/role/{role} - Utilisateurs par rôle
PUT /api/admin/users/{id}/role - Modifier le rôle d'un utilisateur
PUT /api/admin/users/{id}/status - Activer/désactiver un compte
PUT /api/admin/users/{id}/lock - Verrouiller/déverrouiller un compte
GET /api/admin/stats - Statistiques des utilisateurs
```

### Endpoints Influenceur
```
GET /api/influenceur/profile - Profil de l'influenceur connecté
PUT /api/influenceur/profile - Mettre à jour le profil
GET /api/influenceur/stats - Statistiques de l'influenceur
```

## Utilisation

### 1. Inscription
```bash
POST /api/auth/register
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "motdepasse123",
  "confirmPassword": "motdepasse123",
  "phoneNumber": "+22312345678"
}
```

### 2. Connexion
```bash
POST /api/auth/login
{
  "email": "john.doe@example.com",
  "password": "motdepasse123"
}
```

### 3. Utilisation du Token
```bash
Authorization: Bearer <votre_jwt_token>
```

### 4. Renouvellement de Token
```bash
POST /api/auth/refresh
{
  "refreshToken": "<votre_refresh_token>"
}
```

## Configuration

### application.properties
```properties
# JWT Configuration
jwt.secret=votre_clé_secrète_très_longue
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Sécurité
security.max-login-attempts=5
security.account-lockout-duration=30
```

## Bonnes Pratiques Implémentées

### 1. Sécurité des Tokens
- Tokens avec expiration automatique
- Refresh tokens séparés
- Révocation des tokens lors de la déconnexion

### 2. Gestion des Sessions
- Sessions stateless (JWT)
- Pas de stockage de session côté serveur
- Authentification à chaque requête

### 3. Validation des Données
- Validation des entrées utilisateur
- Sanitisation des données
- Gestion des erreurs de validation

### 4. Logging et Monitoring
- Logs de sécurité détaillés
- Traçabilité des actions utilisateur
- Monitoring des tentatives de connexion

### 5. Protection CORS
- Configuration CORS sécurisée
- Headers d'autorisation appropriés
- Support des requêtes cross-origin

## Sécurité Supplémentaire

### 1. Rate Limiting
- Limitation des tentatives de connexion
- Protection contre les attaques par force brute

### 2. Validation des Tokens
- Vérification de la signature
- Validation de l'expiration
- Vérification des autorisations

### 3. Gestion des Erreurs
- Messages d'erreur sécurisés
- Pas d'exposition d'informations sensibles
- Logs d'erreur appropriés

## Utilisateurs par Défaut

L'application crée automatiquement des utilisateurs de démonstration au premier démarrage :

### Comptes de Test
- **ADMIN**: `admin@pneumali.ml` / `admin123`
- **CLIENT**: `client@pneumali.ml` / `client123`
- **INFLUENCEUR**: `influenceur@pneumali.ml` / `influenceur123`

### Utilisation des Comptes de Test
```bash
# Connexion admin
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pneumali.ml","password":"admin123"}'

# Connexion client
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"client@pneumali.ml","password":"client123"}'

# Connexion influenceur
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"influenceur@pneumali.ml","password":"influenceur123"}'
```

## Tests de Sécurité

### 1. Test d'Authentification
```bash
# Test de connexion avec un compte par défaut
curl -X POST http://localhost:9999/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pneumali.ml","password":"admin123"}'

### 2. Test d'Autorisation
```bash
# Test d'accès protégé
curl -X GET http://localhost:9999/api/test/admin \
  -H "Authorization: Bearer <votre_token>"
```

### 3. Test de Validation
```bash
# Test de validation des données
curl -X POST http://localhost:9999/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"invalid-email","password":"123"}'
```

## Maintenance

### 1. Nettoyage Automatique
- Suppression des tokens expirés
- Nettoyage quotidien à 2h du matin
- Logs de maintenance

### 2. Rotation des Clés
- Changement régulier de la clé JWT
- Invalidation des tokens existants
- Procédure de migration

### 3. Monitoring
- Surveillance des tentatives de connexion
- Alertes en cas d'activité suspecte
- Métriques de sécurité

## Support

Pour toute question concernant la sécurité, contactez l'équipe de développement.
