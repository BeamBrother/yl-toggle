import { render } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import SectionTitle from '.';

it('PutAwayMenu snapshot', async () => {
  await (async () => {
    const { asFragment } = render(<SectionTitle title="title" tooltipText="tooltipText" showTooltip={true} />);
    expect(asFragment()).toMatchSnapshot();
  })();
});
