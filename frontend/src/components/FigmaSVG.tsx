import React from "react";

const FigmaSvg: React.FC = () => {
  return (
    <svg
      width="890"
      height="558"
      viewBox="0 0 890 558"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <g filter="url(#filter0_d)">
        <rect x="4" width="882" height="550" fill="#D9D9D9" />
        <rect x="4.5" y="0.5" width="881" height="549" stroke="black" />
      </g>
      <mask id="mask0" fill="white">
        <path d="M281 388C281 383.582 284.582 380 289 380H632C636.418 380 640 383.582 640 388V411C640 415.418 636.418 419 632 419H289C284.582 419 281 415.418 281 411V388Z" />
      </mask>
      <path
        d="M281 388C281 383.582 284.582 380 289 380H632C636.418 380 640 383.582 640 388V411C640 415.418 636.418 419 632 419H289C284.582 419 281 415.418 281 411V388Z"
        fill="#2C2C2C"
      />
      <defs>
        <filter
          id="filter0_d"
          x="0"
          y="0"
          width="890"
          height="558"
          filterUnits="userSpaceOnUse"
          colorInterpolationFilters="sRGB"
        >
          <feFlood floodOpacity="0" result="BackgroundImageFix" />
          <feColorMatrix
            in="SourceAlpha"
            type="matrix"
            values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 127 0"
            result="hardAlpha"
          />
          <feOffset dy="4" />
          <feGaussianBlur stdDeviation="2" />
          <feComposite in2="hardAlpha" operator="out" />
          <feColorMatrix
            type="matrix"
            values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.25 0"
          />
          <feBlend mode="normal" in2="BackgroundImageFix" result="effect1_dropShadow" />
          <feBlend mode="normal" in="SourceGraphic" in2="effect1_dropShadow" result="shape" />
        </filter>
      </defs>
    </svg>
  );
};

export default FigmaSvg;
