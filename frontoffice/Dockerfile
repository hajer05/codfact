FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm cache clean --force && npm install --force

COPY . .
# Vous pouvez remplacer par: ng build --configuration=production
RUN npm run build --prod

FROM nginx:latest

COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copie le build Angular (dist/frontoffice/browser) à la racine Nginx
COPY --from=build /app/dist/frontoffice/browser/ /usr/share/nginx/html

EXPOSE 4201

CMD ["nginx", "-g", "daemon off;"]










