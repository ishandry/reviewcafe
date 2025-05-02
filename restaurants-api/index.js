import express from 'express';

const app = express();
const PORT = process.env.PORT || 3000;

const restaurants = [
  {
    id: "1",
    name: "Café de Flore",
    address: "172 Boulevard Saint-Germain, 75006 Paris, France",
    latitude: 48.855100,
    longitude: 2.333100,
    description: "One of the oldest cafés in Paris with an atmosphere of intellectual discussions and classic French desserts."
  },
  {
    id: "2",
    name: "Ladurée Champs-Élysées",
    address: "75 Avenue des Champs-Élysées, 75008 Paris, France",
    latitude: 48.869800,
    longitude: 2.307000,
    description: "Famous patisserie offering elegant macarons and a charming atmosphere."
  },
  {
    id: "3",
    name: "Le Café Marly",
    address: "93 Rue de Rivoli, 75001 Paris, France",
    latitude: 48.861500,
    longitude: 2.336400,
    description: "Great view of the Louvre and a stylish interior for a high-end leisure experience."
  },
  {
    id: "4",
    name: "Les Deux Magots",
    address: "6 Place Saint-Germain des Prés, 75006 Paris, France",
    latitude: 48.854300,
    longitude: 2.333100,
    description: "Classic café with a history of inspiring new movements in world literature."
  },
  {
    id: "5",
    name: "Angelina",
    address: "226 Rue de Rivoli, 75001 Paris, France",
    latitude: 48.863700,
    longitude: 2.335300,
    description: "World-renowned spot for hot chocolate and exquisite pastries."
  },
  {
    id: "6",
    name: "La Maison Rose",
    address: "2 Rue de l'Abreuvoir, 75018 Paris, France",
    latitude: 48.887600,
    longitude: 2.343100,
    description: "Fairytale-like café in Montmartre with wonderful views and a cozy ambiance."
  },
  {
    id: "7",
    name: "Le Procope",
    address: "13 Rue de l'Ancienne Comédie, 75006 Paris, France",
    latitude: 48.853500,
    longitude: 2.338300,
    description: "The oldest restaurant in Paris, known for its history and classic French cuisine."
  },
  {
    id: "8",
    name: "Le Jules Verne",
    address: "2nd floor, Eiffel Tower, Champ de Mars, 75007 Paris, France",
    latitude: 48.858400,
    longitude: 2.294500,
    description: "Elite restaurant on the second floor of the Eiffel Tower with panoramic views."
  },
  {
    id: "9",
    name: "Le Grand Véfour",
    address: "17 Rue de Beaujolais, 75001 Paris, France",
    latitude: 48.863400,
    longitude: 2.337100,
    description: "Luxurious restaurant in the Palais-Royal gardens with refined gastronomy."
  },
  {
    id: "10",
    name: "Café des Deux Moulins",
    address: "15 Rue Lepic, 75018 Paris, France",
    latitude: 48.884300,
    longitude: 2.332400,
    description: "Charming café in Montmartre, famous from the film 'Amélie'."
  }
];

app.get('/api/restaurants', (req, res) => {
  console.log("request")
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const size = Math.max(parseInt(req.query.size) || 5, 1);
  const start = (page - 1) * size;
  const end = start + size;
  const paged = restaurants.slice(start, end);

  res.json({
    page,
    size,
    total: restaurants.length,
    data: paged
  });
});

app.get('/api/restaurants/:id', (req, res) => {
  console.log("request")
  const id = parseInt(req.params.id);
  const restaurant = restaurants.find(r => r.id === id);

  if (!restaurant) {
    return res.status(404).json({ message: 'Restaurant not found' });
  }

  res.json(restaurant);
});

app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
