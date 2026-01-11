import React from 'react';

interface FiltersProps {
  categories: string[];
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
  resultCount: number;
}

export const Filters: React.FC<FiltersProps> = ({ 
  categories, 
  selectedCategory, 
  onCategoryChange, 
  resultCount 
}) => {
  return (
    <div className="max-w-6xl mx-auto px-4 py-6">
      <div className="flex flex-wrap items-center gap-4">
        <div className="flex-shrink-0">
          <label htmlFor="category-filter" className="sr-only">Filter by category</label>
          <select
            id="category-filter"
            value={selectedCategory}
            onChange={(e) => onCategoryChange(e.target.value)}
            className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {categories.map(cat => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
        </div>
        <p className="text-sm text-gray-600">
          {resultCount} events found
        </p>
      </div>
    </div>
  );
};