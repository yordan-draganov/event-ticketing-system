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
    <div className="flex flex-wrap justify-center gap-3 mb-10">
      {sections.map(section => (
        <button
          key={section.id}
          onClick={() => {
            if (section.id) {
              onSectionChange(section.id);
            }
          }}
          className={`group flex flex-col items-center px-6 py-4 rounded-xl border-2 transition-all ${
            selectedSection === section.id
              ? 'border-blue-600 bg-blue-50'
              : 'border-transparent bg-white hover:border-gray-200'
          }`}
        >
          <span className={`font-bold ${selectedSection === section.id ? 'text-blue-700' : 'text-gray-900'}`}>
            {section.name}
          </span>
          <span className={`text-sm ${selectedSection === section.id ? 'text-blue-600' : 'text-gray-500'}`}>
            ${section.price} • {section.availableSeats} left
          </span>
        </button>
      ))}
    </div>
  );
};

