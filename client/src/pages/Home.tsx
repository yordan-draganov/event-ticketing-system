import { Button, Card, CardContent, TextField, Typography } from "@mui/material";
import React from "react";

interface HomeProps {
  appName: string;
  onSearch: (query: string) => void;
}

const Home: React.FC<HomeProps> = ({ appName, onSearch }) => {
  const [query, setQuery] = React.useState<string>("");

  const handleSearch = (): void => {
    onSearch(query);
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-gray-100 flex flex-col">
      
      {/* Navbar */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-4 flex justify-between items-center">
          <h1 className="text-xl font-bold text-blue-600">{appName}</h1>
          <Button variant="contained">Sign In</Button>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 flex items-center">
        <div className="max-w-7xl mx-auto px-6 w-full text-center">
          
          <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
            Find what you need
          </h2>

          <p className="text-gray-600 mb-8 max-w-2xl mx-auto">
            Search.
          </p>

          {/* Search Bar */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-12">
            <TextField
              fullWidth
              placeholder="Search..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              sx={{ maxWidth: 400, backgroundColor: "white", borderRadius: 1 }}
            />
            <Button
              variant="contained"
              size="large"
              onClick={handleSearch}
            >
              Search
            </Button>
          </div>

          {/* Info Cards */}
          <div className="grid md:grid-cols-3 gap-6">
            <Card>
              <CardContent>
                <Typography variant="h6">Fast</Typography>
                <Typography color="text.secondary">
                  Text
                </Typography>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Typography variant="h6">Modern</Typography>
                <Typography color="text.secondary">
                  Text
                </Typography>
              </CardContent>
            </Card>

            <Card>
              <CardContent>
                <Typography variant="h6">Scalable</Typography>
                <Typography color="text.secondary">
                  Text
                </Typography>
              </CardContent>
            </Card>
          </div>

        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t mt-12">
        <div className="max-w-7xl mx-auto px-6 py-4 text-center text-gray-500">
          © 2026 All rights reserved.
        </div>
      </footer>
    </div>
  );
};

export default Home;
