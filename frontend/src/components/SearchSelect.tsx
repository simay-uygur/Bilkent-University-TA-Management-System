import React, { useState, useRef, useEffect, ChangeEvent } from 'react';
import styles from './SearchSelect.module.css';

export interface SearchSelectProps<T> {
  /** The full list of options to search through */
  options: T[];
  /** How to render each option in the list */
  renderOption: (option: T) => React.ReactNode;
  /** How to derive the string to filter by from each option */
  filterOption: (option: T) => string;
  /** Placeholder text for the input */
  placeholder?: string;
  /** Callback when an option is selected */
  onSelect: (value: T) => void;
  /** Optional additional className on the container */
  className?: string;
}

export function SearchSelect<T>({
  options,
  renderOption,
  filterOption,
  placeholder = 'Search...',
  onSelect,
  className = '',
}: SearchSelectProps<T>) {
  const [query, setQuery] = useState('');
  const [filtered, setFiltered] = useState<T[]>(options);
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const q = query.trim().toLowerCase();
    setFiltered(
      q === ''
        ? options
        : options.filter(opt =>
            filterOption(opt).toLowerCase().includes(q)
          )
    );
  }, [query, options, filterOption]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () =>
      document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div
      className={`${styles.container} ${className}`}
      ref={containerRef}
    >
      <input
        type="text"
        className={styles.input}
        placeholder={placeholder}
        value={query}
        onChange={(e: ChangeEvent<HTMLInputElement>) => {
          setQuery(e.target.value);
          setOpen(true);
        }}
        onFocus={() => setOpen(true)}
      />
      {open && filtered.length > 0 && (
        <ul className={styles.dropdown}>
          {filtered.map((opt, idx) => (
            <li
              key={idx}
              className={styles.item}
              onClick={() => {
                onSelect(opt);
                setQuery(filterOption(opt));
                setOpen(false);
              }}
            >
              {renderOption(opt)}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default SearchSelect;