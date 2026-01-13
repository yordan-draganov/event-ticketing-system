import React from 'react';
import type { SectionResponse } from '../../generated/api';

interface SectionSelectorProps {
  sections: SectionResponse[];
  selectedSection: string | null;
  onSectionChange: (sectionId: string) => void;
}

export const SectionSelector: React.FC<SectionSelectorProps> = ({
  sections,
  selectedSection,
  onSectionChange,
}) => {
  return (
    <div className="mb-8 sm:mb-10">
      <div className="hidden sm:flex flex-wrap justify-center gap-3">
        {sections.map(section => (
          <button
            key={section.id}
            onClick={() => {
              if (section.id) {
                onSectionChange(section.id);
              }
            }}
            className={`group flex flex-col items-center px-4 sm:px-6 py-3 sm:py-4 rounded-xl border-2 transition-all ${
              selectedSection === section.id
                ? 'border-blue-600 bg-blue-50'
                : 'border-transparent bg-white hover:border-gray-200 shadow-sm'
            }`}
          >
            <span className={`font-bold text-sm sm:text-base ${selectedSection === section.id ? 'text-blue-700' : 'text-gray-900'}`}>
              {section.name}
            </span>
            <span className={`text-xs sm:text-sm ${selectedSection === section.id ? 'text-blue-600' : 'text-gray-500'}`}>
              ${section.price} • {section.availableSeats} left
            </span>
          </button>
        ))}
      </div>

      <div className="sm:hidden space-y-2 px-4">
        {sections.map(section => (
          <button
            key={section.id}
            onClick={() => {
              if (section.id) {
                onSectionChange(section.id);
              }
            }}
            className={`w-full flex items-center justify-between p-4 rounded-xl border-2 transition-all ${
              selectedSection === section.id
                ? 'border-blue-600 bg-blue-50'
                : 'border-gray-200 bg-white active:scale-[0.98]'
            }`}
          >
            <div className="flex flex-col items-start">
              <span className={`font-bold text-base ${selectedSection === section.id ? 'text-blue-700' : 'text-gray-900'}`}>
                {section.name}
              </span>
              <span className={`text-sm ${selectedSection === section.id ? 'text-blue-600' : 'text-gray-500'}`}>
                {section.availableSeats} seats available
              </span>
            </div>
            <div className="text-right">
              <div className={`text-xl font-bold ${selectedSection === section.id ? 'text-blue-700' : 'text-gray-900'}`}>
                ${section.price}
              </div>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
};