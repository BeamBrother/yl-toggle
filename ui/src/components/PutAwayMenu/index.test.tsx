import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/vitest';
import userEvent from '@testing-library/user-event';
import PutAwayMenu from '.';
import sleep from 'utils/sleep';

console.error = vi.fn();

it('PutAwayMenu snapshot', async () => {
  await (async () => {
    const { asFragment, rerender } = render(<PutAwayMenu isPutAway={true} title="title" type="type" />);
    expect(asFragment()).toMatchSnapshot();
    rerender(<PutAwayMenu isPutAway={false} title="title" type="type" />);
    expect(asFragment()).toMatchSnapshot();
  })();
});

it('hover PutAwayMenu', async () => {
  await (async () => {
    const { baseElement } = render(<PutAwayMenu isPutAway={true} title="title" type="type" />);
    const ele = screen.getAllByRole('generic').pop();
    expect(ele).not.toBeUndefined();

    if(ele) {
      await userEvent.hover(ele);
      await sleep(1000);
    }

    expect(baseElement).toMatchSnapshot();
  })();
});
